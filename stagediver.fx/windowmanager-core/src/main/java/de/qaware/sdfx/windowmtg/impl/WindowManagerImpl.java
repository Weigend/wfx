package de.qaware.sdfx.windowmtg.impl;

import com.google.common.collect.ImmutableList;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.net.URL;
import java.util.*;

/**
 * Handles the full window management with fully customizable layout and drag&drop into new not existing windows.
 */
public class WindowManagerImpl implements MultiWindowManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowManagerImpl.class);

    private final DragNDropManager dragNDropManager = new DragNDropManagerImpl(this);

    private Map<String, ViewStatus> views = new LinkedHashMap<>();

    private final List<RootArea> subWindows = new ArrayList<>();

    protected Pane rootPane = new HBox();

    private RootArea mainArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initialize the WindowManager");
        dragNDropManager.initialize(url, resourceBundle);
    }

    /**
     * Get the main area. If it not exists it will be created.
     *
     * @return The main area.
     */
    private ViewArea getMainArea() {
        if (mainArea == null) {
            mainArea = new RootArea(rootPane, dragNDropManager, false);
        }
        return mainArea;
    }

    /**
     * Get the root pane for this window manager.
     *
     * @return The root pane.
     */
    @Override
    public Parent getRootPane() {
        return getMainArea().getNode();
    }

    /**
     * Register a new view within this window manager.
     * <p/>
     * The Position will give an advice where this view should be placed.
     *
     * @param view The view to register.
     */
    @Override
    public void register(View view) {
        ViewStatus v = new ViewStatus(view);
        if (views.containsKey(view.getViewId())) {
            ViewStatus oldView = views.get(view.getViewId());
            TabArea area = oldView.getArea();
            area.add(v, Position.CENTER);
            area.remove(oldView);
        }
        else {
            getMainArea().add(v, v.getPosition());
        }
        views.put(view.getViewId(), v);
    }

    /**
     * Register a new view within this window manager using a parent view to define the exact position.
     *
     * @param view   The view to register.
     * @param parent An already registered view which defines the exact position to insert the view.
     */
    @Override
    public void register(View view, View parent) {
        if (!views.containsKey(parent.getViewId())) {
            throw new IllegalArgumentException("Can not find parent view");
        }
        ViewStatus parentStatus = views.get(parent.getViewId());
        ViewStatus viewStatus = new ViewStatus(view, parentStatus);

        if (views.containsKey(view.getViewId())) {
            ViewStatus oldView = views.get(view.getViewId());
            oldView.getArea().remove(viewStatus);
        }
        parentStatus.getArea().add(viewStatus, viewStatus.getPosition());
        views.put(view.getViewId(), viewStatus);
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
     * Remove and close the given root area.
     *
     * @param area The root area to remove.
     */
    @Override
    public void remove(RootArea area) {
        LOGGER.info("Remove existing window");
        List<ViewStatus> views = getForRootArea(area);
        for (ViewStatus view : views) {
            view.getArea().remove(view);
        }
        ((Stage) area.getNode().getScene().getWindow()).close();
        subWindows.remove(area);
    }

    /**
     * Restore the layout to default.
     * <p/>
     * The layout is recreated in the same way as it was the first time initialized.
     */
    @Override
    public void restoreDefaultLayout() {
        mainArea = null;

        for (RootArea subWindow : new ImmutableList.Builder<RootArea>().addAll(subWindows).build()) {
            remove(subWindow);
        }
        rootPane.getChildren().clear();
        Map<String, ViewStatus> views = this.views;
        this.views = new LinkedHashMap<>();
        for (ViewStatus view : views.values()) {
            view.restoreDefault();
            if (view.getParent() != null) {
                register(view.getView(), view.getParent().getView());
            }
            else {
                register(view.getView());
            }
        }
    }

    /**
     * Close the specified view.
     * <p/>
     * The given view must be registered within the {@link de.qaware.sdfx.windowmtg.api.WindowManager}. If it is not registered a
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param view That view that should be closed
     * @throws IllegalArgumentException In case of the view is not registered.
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
     * <p/>
     * The cloned view will be placed next to the given view in the same tab area.
     * <p/>
     * The given view must be registered within the {@link de.qaware.sdfx.windowmtg.api.WindowManager}. If it is not registered a
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param view Clone the given view.
     * @return The cloned view object.
     * @throws IllegalArgumentException In case of the view is not registered.
     */
    @Override
    public View cloneView(View view) {
        return null;
    }

    /**
     * Show a closed view again.
     * The view will be shown at the same position where it was on close.
     * The given view must be registered within the {@link de.qaware.sdfx.windowmtg.api.WindowManager}. If it is not registered a
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param view The view to show.
     * @throws IllegalArgumentException In case of the view is not registered.
     */
    @Override
    public void showView(View view) {
        ViewStatus viewStatus = views.get(view.getViewId());
        if (viewStatus == null || viewStatus.getView() != view) {
            throw new IllegalArgumentException(String.format("View with id '%s' is not registered", view.getViewId()));
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
     * Bring all windows managed by this window manager to front.
     */
    @Override
    public void bringToFront() {

        for (RootArea area : subWindows) {
            if (area.getNode().getScene().getWindow() instanceof Stage) {
                ((Stage) area.getNode().getScene().getWindow()).toFront();
            }
        }
        ((Stage) mainArea.getNode().getScene().getWindow()).toFront();
    }

    /**
     * Get a list with all views which are registered under the given {@link RootArea}
     *
     * @param area The requested root area.
     * @return A list with all views which are registered under the given area.
     */
    private List<ViewStatus> getForRootArea(final RootArea area) {
        List<ViewStatus> areaViews = new ArrayList<>();
        for (ViewStatus view : views.values()) {
            if (view.getArea() != null && view.getArea().getRootArea() == area) {
                areaViews.add(view);
            }
        }
        return areaViews;
    }
}
