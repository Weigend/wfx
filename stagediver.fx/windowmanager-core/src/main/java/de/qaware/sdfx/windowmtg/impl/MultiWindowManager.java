package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.WindowManager;

/**
 *
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
}
