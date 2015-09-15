/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
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
package de.qaware.sdfx.windowmtg.impl;

import com.google.common.collect.ImmutableList;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
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
    private final ReadOnlyListWrapper<RootArea> subWindows = new ReadOnlyListWrapper<>();
    private final ReadOnlyObjectWrapper<RootArea> mainRootArea = new ReadOnlyObjectWrapper<>();
    private final SimpleObjectProperty<View> focusedView = new SimpleObjectProperty<>();
    private Map<String, ViewStatus> views = new LinkedHashMap<>();
    private View lastFocusedView;

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
     * @param view The view to register.
     */
    @Override
    public void register(final View view) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> register(view));
            return;
        }

        ViewStatus v = new ViewStatus(view);
        ViewArea area = getMainRootArea();
        Position position = v.getPosition();
        if (views.containsKey(view.getViewId())) {
            ViewStatus oldView = views.get(view.getViewId());
            TabArea tabArea = oldView.getArea();
            if (tabArea != null && tabArea.isValid()) {
                area = tabArea;
                position = Position.CENTER;
                tabArea.remove(oldView);
            }
        }
        area.add(v, position);
        views.put(view.getViewId(), v);
    }

    /**
     * Register a new view within this window manager using a parent view to define the exact position.
     *
     * @param view   The view to register.
     * @param parent An already registered view which defines the exact position to insert the view.
     */
    @Override
    public void register(final View view, final View parent) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> register(view, parent));
            return;
        }

        if (!views.containsKey(parent.getViewId())) {
            throw new IllegalArgumentException("Can not find parent view");
        }
        ViewStatus parentStatus = views.get(parent.getViewId());
        ViewStatus viewStatus = new ViewStatus(view, parentStatus);

        if (views.containsKey(view.getViewId())) {
            ViewStatus oldView = views.get(view.getViewId());
            oldView.getArea().remove(oldView);
        }
        parentStatus.getArea().add(viewStatus, viewStatus.getPosition());
        views.put(view.getViewId(), viewStatus);
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
        List<RootArea> currentSubwindows = new ImmutableList.Builder<RootArea>().addAll(subWindows).build();
        currentSubwindows.forEach(this::remove);
        rootPane.getChildren().clear();
        //save the old views
        LinkedHashMap<String, ViewStatus> oldViews = new LinkedHashMap<>();
        oldViews.putAll(views);
        this.views = new LinkedHashMap<>();
        for (ViewStatus view : oldViews.values()) {
            view.restoreDefault();
            if (view.getParent() == null) {
                register(view.getView());
            }
            else {
                register(view.getView(), view.getParent().getView());
            }
        }
    }

    /**
     * Close the specified view.
     * <p>
     * The given view must be registered within the {@link de.qaware.sdfx.windowmtg.api.WindowManager}. If it is not registered a
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param view That view that should be closed
     */
    @Override
    public void closeView(View view) {
        if (!views.containsKey(view.getViewId())) {
            throw new IllegalArgumentException(String.format("View with id '%s' is not registered", view.getViewId()));
        }
        ViewStatus viewStatus = views.get(view.getViewId());
        viewStatus.getArea().remove(viewStatus);
        viewStatus.setStatus(ViewStatus.Status.HIDDEN);
    }

    /**
     * Clone the specified view.
     * <p>
     * The cloned view will be placed next to the given view in the same tab area.
     * <p>
     * The given view must be registered within the {@link de.qaware.sdfx.windowmtg.api.WindowManager}. If it is not registered a
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param view Clone the given view.
     * @return The cloned view object.
     */
    @Override
    public View cloneView(View view) {
        return null;
    }

    /**
     * Show a closed or hidden view again.
     * The view will be shown at the same position where it was on close.
     * The given view must be registered within the {@link de.qaware.sdfx.windowmtg.api.WindowManager}. If it is not registered a
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param view The view to show.
     */
    @Override
    public void showView(View view) {
        ViewStatus viewStatus = views.get(view.getViewId());
        if (viewStatus == null || viewStatus.getView() != view) {
            throw new IllegalArgumentException(String.format("View with id '%s' is not registered", view.getViewId()));
        }
        if (viewStatus.getStatus() == ViewStatus.Status.VISIBLE && viewStatus.getTab().getTabPane() != null) {
            viewStatus.getTab().getTabPane().requestFocus();
            viewStatus.getTab().getTabPane().getSelectionModel().select(viewStatus.getTab());
            setFocusedView(viewStatus.getView());
            return;
        }
        viewStatus.restoreDefault();

        ViewStatus parent = viewStatus.getParent();
        boolean added = false;
        views.remove(view.getViewId());

        while (!added && parent != null) {
            if (parent.getStatus() == ViewStatus.Status.VISIBLE) {
                register(view, parent.getView());
                added = true;
            }
            parent = parent.getParent();
        }
        if (!added) {
            register(view);
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
        if (!views.containsKey(viewID)) {
            return null;
        }
        return views.get(viewID).getView();
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
        return views.values().stream()
                .filter(viewStatus -> viewStatus.getTab().isSelected())
                .map(ViewStatus::getView)
                .collect(Collectors.toList());
    }

    /**
     * Set the divider positions for all current views.
     */
    private void setDividerPositions() {
        views.values().forEach(ViewStatus::setDividerPositions);
    }

    /**
     * Get a list with all views which are registered under the given {@link RootArea}
     *
     * @param area The requested root area.
     * @return A list with all views which are registered under the given area.
     */
    private List<ViewStatus> getForRootArea(final RootArea area) {
        return views.values().stream()
                .filter(view -> view.getArea() != null && view.getArea().getRootArea() == area)
                .collect(Collectors.toList());
    }
}
