//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-core
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 29.06.13 21:51
//      description:
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
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 29.06.13
 * Time: 21:51
 * To change this template use File | Settings | File Templates.
 */
public class MainWindowImpl extends AbstractWindow implements MainWindow {

    private MenuBar menuBar = new MenuBar();
    private WindowManager windowManager;
    private ToolBar toolbar = new ToolBar();


    public MainWindowImpl(Stage stage, RootArea rootArea) {
        super(stage, rootArea);
        menuBar.setUseSystemMenuBar(true);
        getRootPane().getChildren().add(0, menuBar);
    }

    @Override
    public ObservableList<Menu> getMenu() {
        return menuBar.getMenus();
    }

    @Override
    public ObservableList<Node> getToolbarItems() {
        return toolbar.getItems();
    }

    @Override
    public WindowManager getWindowManager() {
        return windowManager;
    }

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
