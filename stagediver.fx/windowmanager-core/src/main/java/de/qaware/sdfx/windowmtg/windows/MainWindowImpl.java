//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-core
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 29.06.13 21:51
//      description: The main window of the stagediver.fx platform.
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.windows;

import de.qaware.sdfx.windowmtg.api.MainWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import de.qaware.sdfx.windowmtg.impl.RootArea;

import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;

/**
 * This is the main window of the stagediver.fx platform. It supports the window management
 * and the default bars like menu, tool and status bar.
 */
public class MainWindowImpl extends AbstractWindow implements MainWindow {

    private MenuBar menuBar = new MenuBar();
    private WindowManager windowManager;
    private ToolBar toolbar = new ToolBar();

    /**
     * Create a new main window.
     *
     * @param stage    The stage for this window.
     * @param rootArea The root area.
     */
    public MainWindowImpl(Stage stage, RootArea rootArea) {
        super(stage, rootArea);
        menuBar.setUseSystemMenuBar(true);
        getRootPane().getChildren().add(0, menuBar);
    }

    /**
     * Get a list with all menu items.
     *
     * @return A list with the menu items.
     */
    @Override
    public ObservableList<Menu> getMenu() {
        return menuBar.getMenus();
    }

    /**
     * Get a list with all tool bar items.
     *
     * @return A list with the toolbar items.
     */
    @Override
    public ObservableList<Node> getToolbarItems() {
        return toolbar.getItems();
    }

    /**
     * Get the window manager that is used to manage this window.
     *
     * @return The currently used window manager.
     */
    @Override
    public WindowManager getWindowManager() {
        return windowManager;
    }

    /**
     * Set a new window manager for this window.
     *
     * @param windowManager The window manager.
     */
    @Override
    public void setWindowManager(WindowManager windowManager) {
        Parent rootPane = windowManager.getRootPane();
        if (!getRootPane().getChildren().contains(rootPane)) {
            getRootPane().getChildren().add(rootPane);
            windowManager.initialize(null, null);
        }
        this.windowManager = windowManager;
    }
}
