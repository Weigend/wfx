/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Get the version of this module.
     *
     * @return The version of the module.
     */
    default String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    /**
     * Preload the module while starting the application.
     * <p>
     * It will be executed in an separate thread while showing the splash screen.
     *
     * @throws PlatformException in case of any error while preloading the module.
     */
    void preload() throws PlatformException;

    /**
     * Finally start the application.
     * <p>
     * It is called from the java fx platform thread in an non specific order, while the platform is initializing the
     * main application window. This includes that all modules have executed there preload phase.
     */
    void start();

    /**
     * Stop the module.
     * <p>
     * This method will be called while platform shutdown.
     */
    void stop();
}

