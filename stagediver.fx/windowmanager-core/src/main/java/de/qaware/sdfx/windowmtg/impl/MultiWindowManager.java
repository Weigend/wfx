// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 23.05.13 10:48
//     description: Defines a window manager which can handle multiple windows.
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.WindowManager;

/**
 * A multi window manager.
 * <p>
 * This is a window manager which is able to handle the views within one or more windows.
 *
 * @author Christian Fritz
 */
public interface MultiWindowManager extends WindowManager {

    /**
     * Register a new root area as subwindow.
     *
     * @param area The new root area.
     */
    void register(RootArea area);

    /**
     * Bring all windows managed by this window manager to front.
     */
    void bringToFront();

    /**
     * Remove and close the given root area.
     *
     * @param area The root area to remove.
     */
    void remove(RootArea area);

    /**
     * Get the root area of the main window.
     *
     * @return The root area of the main window
     */
    RootArea getMainRootArea();

    /**
     * Request the redrawing of all areas.
     */
    void redrawAreas();
}
