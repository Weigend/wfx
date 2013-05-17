package de.qaware.sdfx.platform.api;

import de.qaware.sdfx.windowmtg.api.WindowManager;

/**
 *
 */
public interface MainWindow {

    void setTitle(String title);

    void restoreTitle();

    void setWindowManager(WindowManager windowManager);
}
