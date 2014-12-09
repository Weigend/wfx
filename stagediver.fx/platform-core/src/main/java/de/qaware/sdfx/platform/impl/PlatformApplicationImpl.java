// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 04.06.13 21:31
//     description: The JavaFX application for the stagediver.fx platform.
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import de.qaware.sdfx.platform.api.exceptions.PlatformException;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.*;
import javafx.stage.*;
import java.io.IOException;
import java.util.List;

/**
 * The JavaFX application. It initialize the javafx application thread and the main stage for stagediver.fx platform.
 *
 * @author christian.fritz
 */
public class PlatformApplicationImpl implements PlatformApplication {

    public static final String INIT_WINSYSTEM_MSG = "Initialize Window System";
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformApplicationImpl.class);
    private Stage mainApplicationStage;
    private Stage preloaderStage;

    /**
     * Get the human readable module name.
     *
     * @return The module name.
     */
    @Override
    public String getName() {
        return "Platform Core Application.";
    }

    /**
     * Get the version of this module.
     *
     * @return The version of the module.
     */
    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public void start() {
        PreloaderNotificationService notificationService = Lookup.lookup(PreloaderNotificationService.class);
        if (notificationService instanceof PreloaderNotificationServiceImpl) {
            ((PreloaderNotificationServiceImpl) notificationService).setApplication(this);
        }
//        notificationService.sendNotification(PlatformNotificationKeys.CORE, INIT_WINSYSTEM_MSG, 0);
    }

    /**
     * Shutdown the JavaFX application and stop the platform bundle.
     */
    @Override
    public void stop() {
        mainApplicationStage.close();
    }

    /**
     * Show the preloader screen within the given stage.
     *
     * @param stage The stage where the preloader should be shown.
     */
    @Override
    public void showPreloader(Stage stage) throws IOException {
        new PlatformPreloader().start(stage);
        preloaderStage = stage;
    }

    /**
     * Hide the preloader if it is currently visible.
     */
    @Override
    public void hidePreloader() {
        if (preloaderStage != null) {
            preloaderStage.close();
        }
    }

    /**
     * Preload the module while starting the application.
     * <p/>
     * It will be executed in an separate thread while showing the splash screen.
     */
    @Override
    public void preload() {
    }

    /**
     * Request the platform to show the main window.
     *
     * @param stage The stage where the main window will be shown.
     */
    @Override
    public void showMainApplicationWindow(Stage stage) throws PlatformException {
        if (preloaderStage != null && preloaderStage.isShowing()) {
            throw new PlatformException("Can not show main application window while the preloader is visible");
        }
        preloaderStage = null;
        mainApplicationStage = stage;
        WindowManager windowManager = Lookup.lookup(WindowManager.class);
        List<ApplicationWindow> windowList = Lookup.lookupAll(ApplicationWindow.class);
        for (ApplicationWindow window : windowList) {
            try {
                window.setStage(stage);
                window.setWindowManager(windowManager);
                window.init();
                window.getStage().show();
                windowManager.init();

                // Send Init Message
                PreloaderNotificationService notificationService = Lookup.lookup(PreloaderNotificationService.class);
                notificationService.sendNotification(PlatformNotificationKeys.CORE, INIT_WINSYSTEM_MSG, 1);
                break;
            }
            catch (IOException e) {
                LOGGER.debug("Can not load Application Window", e);
            }
        }
    }

    /**
     * Notify the preloader about the preloading progress.
     *
     * @param preloaderNotification The preloader notification
     */
    @Override
    public void notifyPreloader(Preloader.PreloaderNotification preloaderNotification) {
    }

    /**
     * Request the platform to show the main window.
     */
    @Override
    public void showMainStage() {
    }
}
