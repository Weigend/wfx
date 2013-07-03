//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-core
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 29.06.13 21:50
//      description: The abstract sagediver.fx window.
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.windows;

import de.qaware.sdfx.windowmtg.api.View;
import de.qaware.sdfx.windowmtg.api.Window;
import de.qaware.sdfx.windowmtg.impl.RootArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.*;

/**
 * The abstract window. This is the base class for all windows which are used by the
 * stagediver.fx platform.
 */
public abstract class AbstractWindow implements Window {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWindow.class);
    private VBox root = new VBox();
    private HBox statusBar = new HBox();
    private Stage stage;
    private String defaultTitle;
    private RootArea rootArea;

    /**
     * Create a new window with in the given stage and with the given root area.
     *
     * @param stage The stage where this window should be shown.
     */
    protected AbstractWindow(Stage stage) {
        this.stage = stage;
        this.defaultTitle = stage.getTitle();
    }

    protected RootArea getRootArea() {
        return rootArea;
    }

    protected void setRootArea(RootArea rootArea) {
        this.rootArea = rootArea;
        root.getChildren().add(rootArea.getNode());
    }

    protected HBox getStatusBar() {
        return statusBar;
    }

    /**
     * Get the javafx root node for this window.
     *
     * @return The root node.
     */
    protected VBox getRootPane() {
        return root;
    }

    /**
     * Get the stage where this window is currently shown.
     *
     * @return The stage where this window is shown.
     */
    @Override
    public Stage getStage() {
        return stage;
    }

    /**
     * Set the new title for this window.
     *
     * @param title The new title.
     */
    @Override
    public void setTitle(String title) {
        stage.setTitle(title + " | " + defaultTitle);
    }

    /**
     * Get the window for this window.
     *
     * @return The current title.
     */
    @Override
    public String getTitle() {
        return stage.getTitle();
    }

    /**
     * Reset the title to the default title.
     */
    @Override
    public void restoreTitle() {
        stage.setTitle(defaultTitle);
    }

    /**
     * Get a list with all items which are placed within the status bar.
     *
     * @return A list with all status bar items.
     */
    @Override
    public ObservableList<Node> getStatusBarItems() {
        return statusBar.getChildren();
    }

    /**
     * Get that view that holds at the moment the focus and is within this window.
     *
     * @return The view with the current focus.
     */
    @Override
    public View getFocusedView() {
        return rootArea.getFocusedView();
    }

    /**
     * Get that view that holds the focus before the current view (see {@link de.qaware.sdfx.windowmtg.windows.AbstractWindow#getFocusedView()}).
     *
     * @return The view that holds the focus before the current view.
     */
    @Override
    public View getLastFocusedView() {
        return rootArea.getLastFocusedView();
    }

    /**
     * Set the view which should hold the focus now.
     *
     * @param view The view that should hold the focus.
     */
    @Override
    public void setFocusedView(View view) {
        rootArea.setFocusedView(view);
    }
}
