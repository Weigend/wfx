// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 24.01.14 19:38
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.platform.impl;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Preloader;
import javafx.stage.Stage;
import org.osgi.framework.BundleException;

import java.io.IOException;

/**
 * The JavaFX application. It initialize the javafx application thread and the main stage for stagediver.fx platform.
 */
public interface PlatformApplication {
    void init() throws Exception;

    /**
     * Start the JavaFX application. This will include the initialization of the content for the first stage and
     * show the primary window for the stagediver.fx platform.
     *
     * @param stage The primary window stage.
     * @throws java.io.IOException In case of any fxml loading failure.
     */
    void start(Stage stage) throws IOException;

    /**
     * Shutdown the JavaFX application and stop the platform bundle.
     *
     * @throws org.osgi.framework.BundleException In case of this bundle can not be stopped
     */
    void stop() throws BundleException;

    HostServices getHostServices();

    Application.Parameters getParameters();

    void notifyPreloader(Preloader.PreloaderNotification preloaderNotification);

    void showMainStage();
}
