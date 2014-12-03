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
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * The JavaFX application. It initialize the javafx application thread and the main stage for stagediver.fx platform.
 */
public class PlatformApplicationImpl extends Application implements PlatformApplication {

    public static final String INIT_WINSYSTEM_MSG = "Initialize Window System";
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformApplicationImpl.class);
    private Stage mainApplicationStage;
    private boolean shouldShowing;

    @Override
    public void init() {
        PreloaderNotificationService notificationService = Lookup.lookup(PreloaderNotificationService.class);
        if (notificationService instanceof PreloaderNotificationServiceImpl) {
            ((PreloaderNotificationServiceImpl) notificationService).setApplication(this);
        }
        notificationService.sendNotification(PlatformNotificationKeys.CORE, INIT_WINSYSTEM_MSG, 0);
    }

    /**
     * Start the JavaFX application. This will include the initialization of the content for the first stage and
     * show the primary window for the stagediver.fx platform.
     *
     * @param stage The primary window stage.
     */
    @Override
    public void start(final Stage stage) {
        LOGGER.info("Run JavaFX application start method");
        mainApplicationStage = stage;
        if (shouldShowing) {
            showMainStage();
        }
    }

    /**
     * Shutdown the JavaFX application and stop the platform bundle.
     */
    @Override
    public void stop() {
    }

    /**
     * Request the platform to show the main window.
     */
    @Override
    public void showMainStage() {
        if (mainApplicationStage == null || mainApplicationStage.isShowing()) {
            shouldShowing = true;
            return;
        }
        Platform.runLater(() -> {
            WindowManager windowManager = Lookup.lookup(WindowManager.class);
            List<ApplicationWindow> windowList = Lookup.lookupAll(ApplicationWindow.class);
            for (ApplicationWindow window : windowList) {
                try {
                    window.setStage(mainApplicationStage);
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
        });
    }
}
