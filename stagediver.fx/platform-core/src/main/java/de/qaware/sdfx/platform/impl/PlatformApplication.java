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

import javafx.application.Preloader;
import javafx.stage.Stage;
import org.osgi.framework.BundleException;

import java.io.IOException;

/**
 * The JavaFX application. It initialize the javafx application thread and the main stage for stagediver.fx platform.
 */
public interface PlatformApplication {
    /**
     * Init the platform application
     *
     * @throws Exception In case of any init error
     */
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

    /**
     * Notify the preloader about the preloading progress.
     *
     * @param preloaderNotification The preloader notification
     */
    void notifyPreloader(Preloader.PreloaderNotification preloaderNotification);

    /**
     * Request the platform to show the main window.
     */
    void showMainStage();
}
