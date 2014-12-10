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

package de.qaware.sdfx.platform.api;

import de.qaware.sdfx.platform.api.exceptions.PlatformException;
import javafx.application.Preloader;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The platform application. It implements the concrete views of preloader and main application window.
 *
 * @author christian.fritz
 */
public interface PlatformApplication extends Module {

    /**
     * Show the preloader screen within the given stage.
     *
     * @param stage The stage where the preloader should be shown.
     * @throws java.io.IOException In case of any io error. ie. while loading the fxml.
     */
    void showPreloader(Stage stage) throws IOException;

    /**
     * Hide the preloader if it is currently visible.
     */
    void hidePreloader();

    /**
     * Show the main application window within the given stage.
     * It must throw a {@link de.qaware.sdfx.platform.api.exceptions.PlatformException} when the preloader is visible
     * when this method was called.
     *
     * @param stage The stage where the main window should be shown.
     * @throws de.qaware.sdfx.platform.api.exceptions.PlatformException In case of the preloader is visible.
     * @throws java.io.IOException                                      In case of io errors, ie. loading the fxml.
     */
    void showMainApplicationWindow(Stage stage) throws PlatformException, IOException;

    /**
     * Shutdown the JavaFX application and stop the platform bundle.
     */
    void stop();

    /**
     * Notify the preloader about the preloading progress.
     *
     * @param preloaderNotification The preloader notification
     * @deprecated only for compatibility
     */
    @Deprecated
    void notifyPreloader(Preloader.PreloaderNotification preloaderNotification);

    /**
     * Request the platform to show the main window.
     *
     * @deprecated only for compatibility
     */
    @Deprecated
    void showMainStage();
}
