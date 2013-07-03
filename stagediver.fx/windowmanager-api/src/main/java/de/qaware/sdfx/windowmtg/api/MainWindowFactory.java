//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-api
//  ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 30.06.13 12:30
//      description: Factory for initializing the main window.
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.api;

import javafx.stage.*;

/**
 * Initialize the main window.
 */
public interface MainWindowFactory {

    /**
     * Initialize the main window.
     *
     * @param mainStage The main stage for this application.
     * @return The main window.
     */
    MainWindow initMainWindow(Stage mainStage);
}
