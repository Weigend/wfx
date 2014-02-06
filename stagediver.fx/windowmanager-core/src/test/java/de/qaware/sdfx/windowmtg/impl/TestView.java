// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 06.02.14 17:08
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import javafx.scene.Parent;
import javafx.scene.control.Label;

/**
 * A generic Testview.
 */
public class TestView implements View {

    private String id;

    private Position position;

    public TestView(String id, Position position) {
        this.id = id;
        this.position = position;
    }

    @Override
    public String getViewId() {
        return id;
    }

    @Override
    public String getTitle() {
        return id;
    }

    @Override
    public String getToolTipInfo() {
        return id;
    }

    @Override
    public Position getDefaultPosition() {
        return position;
    }

    @Override
    public Parent getRootNode() {
        Label l = new Label("View ID: " + id);
        l.setId(id.toLowerCase());
        return l;
    }

    @Override
    public double getViewAreaSize() {
        return 0.5;
    }
}