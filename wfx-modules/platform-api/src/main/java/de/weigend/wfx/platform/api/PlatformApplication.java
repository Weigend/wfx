/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 Weigend AM
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
package de.weigend.wfx.platform.api;

import de.weigend.wfx.platform.api.exceptions.PlatformException;

import javafx.stage.*;

/**
 * The platform application. It implements the concrete views of preloader and main application window.
 *
 * @author Software-EKG Team
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
     * It must throw a {@link de.weigend.wfx.platform.api.exceptions.PlatformException} when the preloader is visible
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
