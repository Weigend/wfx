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

import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import de.qaware.sdfx.windowmtg.api.MainWindow;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.*;
import javafx.stage.*;
import java.io.IOException;

/**
 * The JavaFX application. It initialize the javafx application thread and the main stage for stagediver.fx platform.
 */
public class PlatformApplication extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformApplication.class);
    private BundleContext context;
    private MainWindow mainWindow;

    @Override
    public void init() throws Exception {
        context = FrameworkUtil.getBundle(getClass()).getBundleContext();
        PreloaderNotificationService pns = context.getService(
                context.getServiceReference(PreloaderNotificationService.class)
        );
        if (pns instanceof PreloaderNotificationServiceImpl) {
            ((PreloaderNotificationServiceImpl) pns).setApplication(this);
        }
    }

    /**
     * Start the JavaFX application. This will include the initialization of the content for the first stage and
     * show the primary window for the stagediver.fx platform.
     *
     * @param stage The primary window stage.
     * @throws IOException In case of any fxml loading failure.
     */
    @Override
    public void start(final Stage stage) throws IOException {
        LOGGER.info("Run JavaFX application start method");
        stage.setTitle("stagediver.fx Platform");

        mainWindow = context.getService(context.getServiceReference(MainWindow.class));
        mainWindow.setStage(stage);
    }

    /**
     * Shutdown the JavaFX application and stop the platform bundle.
     *
     * @throws BundleException In case of this bundle can not be stopped
     */
    @Override
    public void stop() throws BundleException {
        LOGGER.info("Stop JavaFX application");
        FrameworkUtil.getBundle(getClass()).stop();
    }

    /**
     * Request the platform to show the main window.
     */
    protected void showMainStage() {
        if (mainWindow.getStage().isShowing()) {
            return;
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mainWindow.initialize(null, null);
            }
        });
    }
}
