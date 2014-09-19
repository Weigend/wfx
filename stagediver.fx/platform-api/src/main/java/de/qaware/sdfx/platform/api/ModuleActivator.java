package de.qaware.sdfx.platform.api;

import java.util.List;

/**
 * ModuleActivator interface to implement the initialization and the shutdown of a module while the platform starts and stops.
 *
 * @author christian.fritz
 */
public interface ModuleActivator {
    /**
     * Start up the module.
     * <p>
     * This method will be called while platform startup.
     */
    void start();

    /**
     * Stop the module.
     * <p>
     * This method will be called while platform shutdown.
     */
    void stop();

    /**
     * Returns a list of the {@link java.lang.Class} objects of {@link ModuleActivator} which must be started before the
     * {@link ModuleActivator#start()} can be called.
     *
     * @return A list of ModuleActivator class objects or null if the module startup depends on any other modules while platform startup.
     */
    List<Class<? extends ModuleActivator>> getDependsOnStart();

    /**
     * Returns a list of the {@link java.lang.Class} objects of {@link ModuleActivator} which must be not stopped before
     * the {@link ModuleActivator#stop()} can be called.
     *
     * @return A list of ModuleActivator class objects or null if the module shutdown depends on any other modules while platform shutdown.
     */
    List<Class<? extends ModuleActivator>> dependsOnStop();
}

