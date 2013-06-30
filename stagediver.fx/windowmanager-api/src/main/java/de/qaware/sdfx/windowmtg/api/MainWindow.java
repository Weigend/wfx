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

/**
 * Defines the main window of the stagediver.fx platform.
 */
public interface MainWindow extends Window {

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
     * Get the window manager for the plattform.
     *
     * @return The window manager.
     */
    WindowManager getWindowManager();

    /**
     * Set the window manager for the main window instance.
     *
     * @param windowManager The window manager.
     */
    void setWindowManager(WindowManager windowManager);
}
