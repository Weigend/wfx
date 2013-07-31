//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-api
//  ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 29.06.13 20:40
//      description: Defines the main window.
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.api;

import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import java.io.IOException;

/**
 * Defines the main window of the stagediver.fx platform.
 */
public interface ApplicationWindow {

    /**
     * Initialize the application window.
     *
     * @throws IOException In case of any error when loading the fxml files.
     */
    void init() throws IOException;

    /**
     * Get all menus within the menu bar.
     *
     * @return A list with all menus.
     */
    ObservableList<Menu> getMenu();

    /**
     * Get all items which are placed within the tool bar.
     *
     * @return A list with all nodes.
     */
    ObservableList<Node> getToolbarItems();

    /**
     * Get the items which are currently placed in the status bar.
     *
     * @return A list with all items in the status bar.
     */
    ObservableList<Node> getStatusBarItems();

    /**
     * Get the window manager for the plattform.
     *
     * @return The window manager.
     */
    WindowManager getWindowManager();

    /**
     * Add the window manager to the main window.
     *
     * @param windowManager The window manager.
     */
    void setWindowManager(WindowManager windowManager);

    /**
     * Restore the default title.
     */
    void restoreTitle();

    /**
     * Get the current title of this window.
     *
     * @return The title.
     */
    String getTitle();

    /**
     * Set the new title for this window.
     *
     * @param title The new title.
     */
    void setTitle(String title);

    /**
     * Get the the javafx stage for this window.
     *
     * @return The stage where this window is shown.
     */
    Stage getStage();

    /**
     * Set the javafx stage for this window.
     *
     * @param stage The stage for this window.
     */
    void setStage(Stage stage);
}
