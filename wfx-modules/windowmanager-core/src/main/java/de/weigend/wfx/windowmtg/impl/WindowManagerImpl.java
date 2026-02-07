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
package de.weigend.wfx.windowmtg.impl;

import de.weigend.wfx.windowmtg.api.Position;
import de.weigend.wfx.windowmtg.api.View;
import de.weigend.wfx.windowmtg.api.WindowFactory;
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

import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the full window management with fully customizable layout and drag&drop into new not existing windows.
 *
 * @author christian.fritz
 */
@Singleton
public class WindowManagerImpl implements MultiWindowManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowManagerImpl.class);
    protected Pane rootPane = new HBox();
    private final DragNDropManager dragNDropManager = new DragNDropManagerImpl(this);
    private final ReadOnlyListWrapper<RootArea> subWindows = new ReadOnlyListWrapper<>(this, "subWindows");
    private final ReadOnlyObjectWrapper<RootArea> mainRootArea = new ReadOnlyObjectWrapper<>(this, "mainRootArea");
    private final SimpleObjectProperty<View> focusedView = new SimpleObjectProperty<>(this, "focusedView");
    private Map<String, ViewStatus> viewsStatus = new LinkedHashMap<>();
    private ReadOnlyListWrapper<View> views = new ReadOnlyListWrapper<>(this, "views", FXCollections.observableArrayList());

    private View lastFocusedView;
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
        if (show && viewArea != null && position != null) {
            viewArea.add(viewStatus, position);
        }
        viewsStatus.put(viewStatus.getView().getViewId(), viewStatus);
        views.add(viewStatus.getView());
    }

    @Override
    public boolean unregister(View view) {
        if (!viewsStatus.containsKey(view.getViewId())) {
            return false;
        }
        unregisterImpl(view);
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
     * Restore the layout to default.
     * <p>
     * The layout is recreated in the same way as it was the first time initialized.
     */
    @Override
    public void restoreDefaultLayout() {
        mainRootArea.set(null);
        List<RootArea> currentSubwindows = new ArrayList<>(subWindows);
        currentSubwindows.forEach(this::remove);
        rootPane.getChildren().clear();
        //save the old views
        LinkedHashMap<String, ViewStatus> oldViews = new LinkedHashMap<>();
        oldViews.putAll(viewsStatus);
        viewsStatus.clear();
        views.clear();
        for (ViewStatus view : oldViews.values()) {
            view.restoreDefault();
            if (view.getParent() == null) {
                register(view.getView());
            }
            else {
                register(view.getView(), view.getParent().getView());
            }
        }
        setDividerPositions();
    }

    /**
     * Close the specified view.
     * <p>
     * The given view must be registered within the {@link de.weigend.wfx.windowmtg.api.WindowManager}. If it is not
     * registered a {@link IllegalArgumentException} will be thrown.
     *
     * @param view That view that should be closed
     */
    @Override
    public boolean closeView(View view) {
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
     * view must be registered within the {@link de.weigend.wfx.windowmtg.api.WindowManager}. If it is not registered a
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
     *
     * @param focusedView The view that should hold the focus.
     */
    @Override
    public void setFocusedView(View focusedView) {
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
        viewsStatus.values().forEach(ViewStatus::setDividerPositions);
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
