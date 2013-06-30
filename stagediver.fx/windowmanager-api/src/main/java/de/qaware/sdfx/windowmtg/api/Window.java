//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-api
//  ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 29.06.13 20:51
//      description: Defines a window
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.api;

import javafx.collections.*;
import javafx.scene.*;
import javafx.stage.*;

/**
 * Defines a window of the stagediver.fx platform.
 */
public interface Window {
    void setTitle(String title);

    String getTitle();

    /**
     * Restore the default title.
     */
    void restoreTitle();

    /**
     * Get the items which are currently placed in the status bar.
     *
     * @return A list with all items in the status bar.
     */
    ObservableList<Node> getStatusBarItems();

    /**
     * Get that view that currently holds the focus within this window.
     *
     * @return That view that holds the focus.
     */
    View getFocusedView();

    /**
     * Get that view that holds recently the focus within this window..
     *
     * @return That view that hodls recently the focus.
     */
    View getLastFocusedView();

    /**
     * Set the given view as the view that holds currently the focus.
     *
     * @param view The view that should hold the focus.
     * @throws IllegalArgumentException In case of the given view is currently not
     *                                  placed within this window
     */
    void setFocusedView(View view);

    Stage getStage();
}
