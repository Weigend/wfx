/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 Weigend AM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.softwareecg.wfx.windowmanager.impl;

import io.softwareecg.wfx.windowmanager.api.Position;
import io.softwareecg.wfx.windowmanager.api.View;
import io.softwareecg.wfx.windowmanager.api.ViewKind;
import io.softwareecg.wfx.windowmanager.api.WindowFactory;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the full window management with fully customizable layout and drag&drop into new not existing windows.
 *
 */
@Singleton
public class WindowManagerImpl implements MultiWindowManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowManagerImpl.class);
    protected Pane rootPane = new HBox();
    private final DragNDropManager dragNDropManager = new DragNDropManagerImpl(this);
    private final ReadOnlyListWrapper<RootArea> subWindows = new ReadOnlyListWrapper<>(this, "subWindows");
    private final ReadOnlyObjectWrapper<RootArea> mainRootArea = new ReadOnlyObjectWrapper<>(this, "mainRootArea");
    private final SimpleObjectProperty<View> focusedView = new SimpleObjectProperty<>(this, "focusedView");
    private final Map<String, ViewStatus> viewsStatus = new LinkedHashMap<>();
    private final ReadOnlyListWrapper<View> views = new ReadOnlyListWrapper<>(this, "views", FXCollections.observableArrayList());
    /**
     * Insertion-ordered registry of {@link ViewKind#TOOL} views. Survives
     * close (hide) operations so the auto-built View menu and
     * {@link #restoreDefaultLayout()} have a stable list to work from.
     * Cleared only by an explicit {@link #unregister(View)} or rebuilt by
     * {@link #restoreDefaultLayout()}.
     */
    private final LinkedHashMap<String, ViewStatus> toolViewsByID = new LinkedHashMap<>();
    private final ReadOnlyListWrapper<View> toolViews = new ReadOnlyListWrapper<>(this, "toolViews", FXCollections.observableArrayList());

    private View lastFocusedView;
    private boolean restoringLayout;
    private WindowFactory windowFactory = Stage::new;

    /**
     * Initialize a new window manager.
     */
    public WindowManagerImpl() {
        subWindows.set(FXCollections.observableArrayList());
    }

    /**
     * Called to initialize a controller after its root element has been completely processed.
     */
    public void init() {
        LOGGER.info("Initialize the WindowManager");
        dragNDropManager.init();
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::setDividerPositions);
        }
        else {
            setDividerPositions();
        }
    }

    /**
     * Register a new view within this window manager.
     * <p>
     * The Position will give an advice where this view should be placed.
     *
     * @param view     The view to register.
     * @param showView True if the view should be shown immediately, false otherwise
     */
    @Override
    public void register(final View view, boolean showView) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> register(view));
            return;
        }
        ViewStatus v = new ViewStatus(view);

        ViewArea area = getMainRootArea();
        boolean show = showView;
        Position position = v.getPosition();
        TabArea oldArea = unregisterImpl(view);
        if (oldArea != null && oldArea.isValid()) {
            area = oldArea;
            position = Position.CENTER;
            show = true;
        }
        register(v, show, area, position);
    }

    /**
     * Register a new view within this window manager using a parent view to define the exact position.
     *
     * @param view     The view to register.
     * @param parent   An already registered view which defines the exact position to insert the view.
     * @param showView True if the view should be shown immediately, false otherwise
     */
    @Override
    public void register(final View view, final View parent, boolean showView) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> register(view, parent));
            return;
        }
        if (!viewsStatus.containsKey(parent.getViewId())) {
            throw new IllegalArgumentException("Can not find parent view");
        }

        ViewStatus parentStatus = viewsStatus.get(parent.getViewId());
        ViewStatus viewStatus = new ViewStatus(view, parentStatus);

        boolean show = unregister(view) || showView;
        ViewArea area = findAreaToAdd(parentStatus);
        if (area == null) {
            area = getMainRootArea();
        }
        register(viewStatus, show, area, viewStatus.getPosition());
    }

    /**
     * Find the deepest view area defined by the parent view.
     *
     * @param parentView start the search for a visible parent view area.
     * @return the found view area to add the view. otherwise false.
     */
    private ViewArea findAreaToAdd(ViewStatus parentView) {
        ViewStatus parent = parentView;

        while (parent != null) {
            TabArea area = parent.getArea();
            if (area != null) {
                return area;
            }
            parent = parent.getParent();
        }
        return null;
    }

    /**
     * Performs the real registration by updating the caches and the view area.
     *
     * @param viewStatus the view status to register
     * @param show       should show the view
     * @param viewArea   the real target view area
     * @param position   the real target position.
     */
    private void register(ViewStatus viewStatus, boolean show, ViewArea viewArea, Position position) {
        // Publish the view in viewsStatus/views BEFORE attaching it to the area.
        // TabArea.add() fires its selectedItemProperty listener synchronously
        // when the tab pane was previously empty, which calls back into
        // setFocusedView(). Listeners on focusedViewProperty (e.g. side panels
        // that bind to the focused view's properties) routinely look the view
        // up via findView() / getRegisteredViews() — so the registry must be
        // visible before the focus event fires, otherwise those listeners see
        // a half-registered view and skip the bind.
        viewsStatus.put(viewStatus.getView().getViewId(), viewStatus);
        views.add(viewStatus.getView());
        // Track TOOL views in a stable registry: putIfAbsent means the very
        // first registration wins, subsequent re-registers (e.g. after a
        // close/showView round-trip, or during restoreDefaultLayout) are
        // no-ops on the registry. DOCUMENT views are never tracked here.
        if (viewStatus.getView().getKind() == ViewKind.TOOL) {
            if (toolViewsByID.putIfAbsent(viewStatus.getView().getViewId(), viewStatus) == null) {
                toolViews.add(viewStatus.getView());
            }
        }
        else if (viewStatus.getView().getKind() == ViewKind.DOCUMENT) {
            // Closing a DOCUMENT tab via the X button must fully unregister
            // it. The default tab handler in ViewStatus only sets HIDDEN —
            // good for TOOLs, leaks ViewStatus instances for DOCUMENTs.
            viewStatus.setOnTabClosed(vs -> unregister(vs.getView()));
        }
        boolean shown = false;
        if (show && viewArea != null && position != null) {
            viewArea.add(viewStatus, position);
            shown = true;
        }
        setDividerPositions();
        // A freshly shown view must own the logical focus. If TabArea.add()
        // already fired setFocusedView via its selection listener this is a
        // no-op (setFocusedView is idempotent for same-value calls); otherwise
        // it covers cases where the area's add path does not auto-focus.
        // Suppressed during restoreDefaultLayout so that bulk re-registration
        // does not surf focus through every restored view.
        if (shown && !restoringLayout) {
            setFocusedView(viewStatus.getView());
        }
    }

    @Override
    public boolean unregister(View view) {
        if (!viewsStatus.containsKey(view.getViewId())) {
            return false;
        }
        unregisterImpl(view);
        // Explicit unregister also evicts the view from the tool registry.
        // closeView on a TOOL only hides; unregister is the "gone for good"
        // path that should also strip it from the auto View menu.
        if (toolViewsByID.remove(view.getViewId()) != null) {
            toolViews.remove(view);
        }
        return true;
    }

    /**
     * Unregister a given view.
     * <p>
     * If the view visible it will be closed before unregister.
     *
     * @param view The view to unregister.
     * @return The last position if the view was visible at last. otherwise null.
     */
    private TabArea unregisterImpl(View view) {
        TabArea oldArea = closeViewImpl(view);
        ViewStatus status = viewsStatus.remove(view.getViewId());
        if (status != null) {
            views.remove(status.getView());
        }
        return oldArea;
    }

    /**
     * Get the root pane for this window manager.
     *
     * @return The root pane.
     */
    @Override
    public Parent getRootPane() {
        return getMainRootArea().getNode();
    }

    /**
     * Restore the layout to its default — the canonical "fresh workshop"
     * action on the Window menu.
     * <p>
     * Every {@link ViewKind#DOCUMENT} view is closed and unregistered.
     * Every {@link ViewKind#TOOL} view from the tool registry is
     * re-registered in its original parent / position, even tools that the
     * user had previously closed (the close was a hide, the registry kept
     * the bookkeeping). Subwindows are collapsed into the main root area.
     */
    @Override
    public void restoreDefaultLayout() {
        // Snapshot the tool registry FIRST — once we clear viewsStatus the
        // ViewStatus references in toolViewsByID are still valid (positions,
        // parent linkage) and that's what we replay onto the fresh canvas.
        List<ViewStatus> toolSnapshot = new ArrayList<>(toolViewsByID.values());

        // Remember which views were actually showing (in a tab area) before
        // the layout is torn down.  area != null means the view's node is
        // currently attached to the scene graph; area == null means it was
        // registered but hidden (e.g. registered with showView=false and never
        // opened by the user).  We must capture this BEFORE any cleanup code
        // that might null out the area references.
        Map<String, Boolean> wasShowing = toolSnapshot.stream()
                .collect(Collectors.toMap(
                        vs -> vs.getView().getViewId(),
                        vs -> vs.getArea() != null));

        List<RootArea> currentSubwindows = new ArrayList<>(subWindows);
        currentSubwindows.forEach(this::remove);
        rootPane.getChildren().clear();
        viewsStatus.clear();
        views.clear();
        // Wipe the tool registry as well — the register loop below will
        // re-populate it via the public register path with FRESH ViewStatus
        // instances, keeping the registry consistent with viewsStatus.
        toolViewsByID.clear();
        toolViews.clear();
        // Swap mainRootArea atomically; setting it to null first would fire
        // listeners (e.g. ViewFocusHandler) with newValue == null.
        mainRootArea.set(new RootArea(rootPane, dragNDropManager, false));
        restoringLayout = true;
        try {
            for (ViewStatus view : toolSnapshot) {
                view.restoreDefault();
                boolean show = wasShowing.getOrDefault(view.getView().getViewId(), false);
                if (view.getParent() == null) {
                    register(view.getView(), show);
                }
                else {
                    register(view.getView(), view.getParent().getView(), show);
                }
            }
        }
        finally {
            restoringLayout = false;
        }
        setDividerPositions();
    }

    /**
     * Close the specified view.
     * <p>
     * The given view must be registered within the {@link io.softwareecg.wfx.windowmanager.api.WindowManager}. If it is not
     * registered a {@link IllegalArgumentException} will be thrown.
     *
     * @param view That view that should be closed
     */
    @Override
    public boolean closeView(View view) {
        ViewStatus status = viewsStatus.get(view.getViewId());
        if (status == null) {
            return false;
        }
        // DOCUMENT views are transient: closing means "done with this
        // content", so dispose the registration entirely. TOOL views keep
        // their historical hide-only behaviour so the user can reopen them
        // from the View menu.
        if (view.getKind() == ViewKind.DOCUMENT) {
            return unregister(view);
        }
        return closeViewImpl(view) != null;
    }

    /**
     * Close the given view. It returns the area of the last position.
     * <p>
     * If the view can not be found or is already closed it returns null.
     *
     * @param view the view to close.
     * @return the last position.
     */
    private TabArea closeViewImpl(View view) {
        if (!viewsStatus.containsKey(view.getViewId())) {
            return null;
        }
        ViewStatus viewStatus = viewsStatus.get(view.getViewId());
        viewStatus.setStatus(ViewStatus.Status.HIDDEN);
        TabArea tabArea = viewStatus.getArea();
        if (tabArea != null) {
            tabArea.remove(viewStatus);
        }
        return tabArea;
    }

    /**
     * Show a closed or hidden view again. The view will be shown at the same position where it was on close. The given
     * view must be registered within the {@link io.softwareecg.wfx.windowmanager.api.WindowManager}. If it is not registered a
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param view The view to show.
     */
    @Override
    public void showView(View view) {
        ViewStatus viewStatus = viewsStatus.get(view.getViewId());
        if (viewStatus == null || viewStatus.getView() != view) {
            throw new IllegalArgumentException(String.format("View with id '%s' is not registered", view.getViewId()));
        }
        // view is already registered and visible but in background => request focus on view
        if (viewStatus.getStatus() == ViewStatus.Status.VISIBLE && viewStatus.getTab().getTabPane() != null) {
            viewStatus.getTab().getTabPane().requestFocus();
            viewStatus.getTab().getTabPane().getSelectionModel().select(viewStatus.getTab());
            setFocusedView(viewStatus.getView());
            return;
        }
        viewStatus.restoreDefault();

        ViewStatus parent = viewStatus.getParent();
        if (parent != null) {
            register(view, parent.getView(), true);
        }
        else {
            register(view, true);
        }
    }

    /**
     * Find a view with the assigned view id.
     * <p>
     * This returns that view that has the given unique view id. If there is no view found it returns null.
     *
     * @param viewID The view id to search.
     * @return The registered view or null if it was not found.
     */
    @Override
    public View findView(String viewID) {
        if (!viewsStatus.containsKey(viewID)) {
            return null;
        }
        return viewsStatus.get(viewID).getView();
    }

    @Override
    public View getFocusedView() {
        return focusedView.get();
    }

    @Override
    public View getLastFocusedView() {
        return lastFocusedView;
    }

    /**
     * Set the view that holds currently the focus and updates the last focused view.
     * <p>
     * Idempotent for same-value calls: if {@code focusedView} is already the
     * current focused view, this is a no-op. Without this guard, double
     * invocation (e.g. TabArea's selection listener firing during register
     * followed by the explicit register-time focus call) would overwrite
     * lastFocusedView with the current view and lose the actual previous
     * focus.
     *
     * @param focusedView The view that should hold the focus.
     */
    @Override
    public void setFocusedView(View focusedView) {
        if (this.focusedView.get() == focusedView) {
            return;
        }
        this.lastFocusedView = this.focusedView.get();
        this.focusedView.set(focusedView);
    }

    @Override
    public ObjectProperty<View> focusedViewProperty() {
        return focusedView;
    }

    /**
     * Register a new root area as subwindow.
     *
     * @param area The new root area.
     */
    @Override
    public void register(RootArea area) {
        LOGGER.info("Register a new window");
        subWindows.add(area);
    }

    /**
     * Bring all windows managed by this window manager to front.
     */
    @Override
    public void bringToFront() {
        subWindows.stream()
                .filter(area -> area.getNode().getScene().getWindow() instanceof Stage)
                .forEach(area -> ((Stage) area.getNode().getScene().getWindow()).toFront());
        ((Stage) mainRootArea.get().getNode().getScene().getWindow()).toFront();
    }

    /**
     * Remove and close the given root area.
     *
     * @param area The root area to remove.
     */
    @Override
    public void remove(RootArea area) {
        LOGGER.info("Remove existing window");
        for (ViewStatus view : getForRootArea(area)) {
            view.getArea().remove(view);
        }
        ((Stage) area.getNode().getScene().getWindow()).close();
        subWindows.remove(area);
    }

    /**
     * Get the main area. If it not exists it will be created.
     *
     * @return The main area.
     */
    @Override
    public RootArea getMainRootArea() {
        if (mainRootArea.get() == null) {
            mainRootArea.set(new RootArea(rootPane, dragNDropManager, false));
        }
        return mainRootArea.get();
    }

    @Override
    public ReadOnlyObjectProperty<RootArea> mainRootAreaProperty() {
        return mainRootArea.getReadOnlyProperty();
    }

    @Override
    public ObservableList<RootArea> getRootAreas() {
        return subWindows.getReadOnlyProperty();
    }

    @Override
    public void redrawAreas() {
        LOGGER.debug("Redrawing of areas requested");
        getRootPane().requestLayout();
        for (RootArea area : subWindows) {
            area.getNode().requestLayout();
        }
    }

    @Override
    public List<View> getVisibleViews() {
        return viewsStatus.values().stream()
                .filter(viewStatus -> viewStatus.getTab().isSelected())
                .map(ViewStatus::getView)
                .collect(Collectors.toList());
    }

    @Override
    public ReadOnlyListProperty<View> getRegisteredViews() {
        return views.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyListProperty<View> getToolViews() {
        return toolViews.getReadOnlyProperty();
    }

    @Override
    public WindowFactory getWindowFactory() {
        return windowFactory;
    }

    @Override
    public void setWindowFactory(WindowFactory windowFactory) {
        this.windowFactory = windowFactory;
    }

    @Override
    public boolean hasRegisteredView(View view) {
        return viewsStatus.containsKey(view.getViewId()) && viewsStatus.get(view.getViewId()).getView() == view;
    }

    @Override
    public boolean hasVisibleView(View view) {
        return getVisibleViews().contains(view);
    }

    /**
     * Set the divider positions for all current views.
     */
    private void setDividerPositions() {
        // Only views that are actually attached can have a divider position;
        // closed/detached views (area == null) would otherwise cause a useless
        // sweep through the chain to a SplitPane that no longer holds them.
        viewsStatus.values().stream()
                .filter(vs -> vs.getArea() != null)
                .forEach(ViewStatus::setDividerPositions);
    }

    /**
     * Get a list with all views which are registered under the given {@link RootArea}
     *
     * @param area The requested root area.
     * @return A list with all views which are registered under the given area.
     */
    private List<ViewStatus> getForRootArea(final RootArea area) {
        return viewsStatus.values().stream()
                .filter(view -> view.getArea() != null && view.getArea().getRootArea() == area)
                .collect(Collectors.toList());
    }
}
