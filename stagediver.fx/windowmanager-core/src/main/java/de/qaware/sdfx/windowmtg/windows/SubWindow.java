//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-core
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 30.06.13 11:49
//      description: This is a sub window of the stagediver.fx platform.
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.windows;

import de.qaware.sdfx.windowmtg.impl.RootArea;

import javafx.stage.*;

/**
 * This defines a subwindow of the stagediver.fx platform. It do not have a menu bar or a
 * toolbar and the views of this window are managed by the window manager of the main window.
 */
public class SubWindow extends AbstractWindow {

    /**
     * Create a new sub window.
     *
     * @param stage    The stage where this window should be shown.
     * @param rootArea The root area for the window management.
     */
    public SubWindow(Stage stage, RootArea rootArea) {
        super(stage);
        setRootArea(rootArea);
    }
}
