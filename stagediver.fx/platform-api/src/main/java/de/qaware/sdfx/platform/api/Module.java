package de.qaware.sdfx.platform.api;

import de.qaware.sdfx.platform.api.exceptions.PlatformException;

/**
 * Module interface to implement the initialization and the shutdown of a module while the platform starts and stops.
 *
 * @author christian.fritz
 */
public interface Module {

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
     * <p/>
     * It will be executed in an separate thread while showing the splash screen.
     *
     * @throws PlatformException in case of any error while preloading the module.
     */
    void preload() throws PlatformException;

    /**
     * Finally start the application.
     * <p/>
     * It is called from the java fx platform thread in an non specific order, while the platform is initializing the main
     * application window. This includes that all modules have executed there preload phase.
     */
    void start();

    /**
     * Stop the module.
     * <p/>
     * This method will be called while platform shutdown.
     */
    void stop();
}

