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
import javafx.stage.Stage;

/**
 * The platform application. It implements the concrete views of preloader and main application window.
 *
 * @author christian.fritz
 */
public interface PlatformApplication {

    /**
     * Get the human readable module name.
     *
     * @return The module name.
     */
    String getName();

    /**
     * Get the version of this module.
     *
     * @return The version of the module.
     */
    String getVersion();

    /**
     * Preload the module while starting the application.
     * <p>
     * It will be executed in an separate thread while showing the splash screen.
     *
     * @throws PlatformException In case of any errors starting the platform.
     */
    void preload() throws PlatformException;

    /**
     * Finally start the application.
     * <p>
     * It is called from the java fx platform thread in an non specific order, while the platform is initializing the main
     * application window. This includes that all modules have executed there preload phase.
     *
     * @throws PlatformException In case of any errors starting the platform.
     */
    void start() throws PlatformException;

    /**
     * Show the preloader screen within the given stage.
     *
     * @param stage The stage where the preloader should be shown.
     * @throws PlatformException In case of any error. ie. while loading the fxml.
     */
    void showPreloader(Stage stage) throws PlatformException;

    /**
     * Hide the preloader if it is currently visible.
     *
     * @throws PlatformException In case of any erros while closing the preloader.
     */
    void hidePreloader() throws PlatformException;

    /**
     * Show the main application window within the given stage.
     * It must throw a {@link de.qaware.sdfx.platform.api.exceptions.PlatformException} when the preloader is visible
     * when this method was called.
     *
     * @param stage The stage where the main window should be shown.
     * @throws PlatformException In case of the preloader is visible, or fxml can not be loaded.
     */
    void showMainApplicationWindow(Stage stage) throws PlatformException;

    /**
     * Shutdown the JavaFX application and stop the platform bundle.
     *
     * @throws PlatformException In case of any errors stopping the platform.
     */
    void stop() throws PlatformException;
}
