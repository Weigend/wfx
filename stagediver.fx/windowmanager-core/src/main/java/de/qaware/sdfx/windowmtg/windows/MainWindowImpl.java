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
import de.qaware.sdfx.windowmtg.impl.MultiWindowManager;
import de.qaware.sdfx.windowmtg.impl.WindowManagerImpl;

import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * This is the main window of the stagediver.fx platform. It supports the window management
 * and the default bars like menu, tool and status bar.
 */
public class MainWindowImpl extends AbstractWindow implements MainWindow {

    private MenuBar menuBar = new MenuBar();
    private MultiWindowManager windowManager = new WindowManagerImpl();
    private ToolBar toolbar = new ToolBar();

    /**
     * Create a new main window.
     *
     * @param stage The stage for this window.
     */
    public MainWindowImpl(Stage stage) {
        super(stage);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        menuBar.setUseSystemMenuBar(true);
        getRootPane().getChildren().clear();
        getRootPane().getChildren().add(menuBar);
        getRootPane().getChildren().add(toolbar);
        setRootArea(windowManager.getMainRootArea());
        getRootPane().getChildren().add(getStatusBar());
        getRootPane().setPrefWidth(800);
        getRootPane().setPrefHeight(600);
        getStage().setScene(new Scene(getRootPane()));
        if (!getStage().isShowing()) {
            getStage().show();
        }
        windowManager.initialize(url, resourceBundle);
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
}
