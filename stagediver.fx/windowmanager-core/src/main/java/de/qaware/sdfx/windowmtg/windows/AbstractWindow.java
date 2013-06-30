//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-core
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 29.06.13 21:50
//      description:
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
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 29.06.13
 * Time: 21:50
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractWindow implements Window {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWindow.class);


    private VBox root = new VBox();
    private HBox statusBar = new HBox();
    private Stage stage;
    private String defaultTitle;
    private RootArea rootArea;


    protected AbstractWindow(Stage stage, RootArea rootArea) {
        this.stage = stage;
        this.defaultTitle = stage.getTitle();
        this.rootArea = rootArea;
        root.getChildren().add(rootArea.getNode());
    }

    protected VBox getRootPane() {
        return root;
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void setTitle(String title) {
        stage.setTitle(title + " | " + defaultTitle);
    }

    @Override
    public String getTitle() {
        return stage.getTitle();
    }

    @Override
    public void restoreTitle() {
        stage.setTitle(defaultTitle);
    }

    @Override
    public ObservableList<Node> getStatusBarItems() {
        return statusBar.getChildren();
    }

    @Override
    public View getFocusedView() {
        return rootArea.getFocusedView();
    }

    @Override
    public View getLastFocusedView() {
        return rootArea.getLastFocusedView();
    }

    @Override
    public void setFocusedView(View view) {
        rootArea.setFocusedView(view);
    }
}
