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
package io.softwareecg.wfx.platform.impl;

import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.platform.api.EventBus;
import io.softwareecg.wfx.platform.api.EventBusListener;
import io.softwareecg.wfx.platform.api.PlatformApplication;
import io.softwareecg.wfx.platform.api.events.ProgressEvent;
import io.softwareecg.wfx.platform.api.events.StartupProgressEvent;
import io.softwareecg.wfx.platform.api.exceptions.PlatformException;
import io.softwareecg.wfx.windowmtg.api.ApplicationWindow;
import io.softwareecg.wfx.windowmtg.api.WindowManager;
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * The JavaFX application. It initialize the javafx application thread and the main stage for wfx platform.
 *
 */
@Singleton
@Priority(Integer.MIN_VALUE)
public class PlatformApplicationImpl implements PlatformApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformApplicationImpl.class);
    private Stage mainApplicationStage;
    private Stage preloaderStage;
    private ProgressController progressController;
    private EventBusListener<StartupProgressEvent> startupProgressListener;
    private EventBus<ProgressEvent> eventBus;

    /**
     * Get the EventBus instance (lazy initialization).
     * @return The EventBus for progress events.
     */
    @SuppressWarnings("unchecked")
    private EventBus<ProgressEvent> getEventBus() {
        if (eventBus == null) {
            eventBus = Lookup.lookup(EventBus.class);
        }
        return eventBus;
    }

    /**
     * Get the human readable module name.
     *
     * @return The module name.
     */
    @Override
    public String getName() {
        return "Platform Core Application";
    }

    /**
     * Get the version of this module.
     *
     * @return The version of the module.
     */
    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public void start() {
    }

    /**
     * Shutdown the JavaFX application and stop the platform bundle.
     */
    @Override
    public void stop() {
        mainApplicationStage.close();
    }

    /**
     * Show the preloader screen within the given stage.
     *
     * @param stage The stage where the preloader should be shown.
     * @throws PlatformException In case of errors while loading the fxml.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void showPreloader(Stage stage) throws PlatformException {
        try {
            URL splashFxmlUrl = findSplashScreen();
            FXMLLoader loader = Lookup.lookup(FXMLLoader.class);
            loader.setLocation(splashFxmlUrl);
            Parent parent = loader.load();
            progressController = loader.getController();
            progressController.unsubscribeProgressEvent();
            startupProgressListener = event ->
                    progressController.progress(
                        new ProgressEvent(event.getMessage(), event.getProgress(), event.getSource()));
            EventBus<StartupProgressEvent> bus = Lookup.lookup(EventBus.class);
            bus.subscribe(StartupProgressEvent.class, startupProgressListener);
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.show();
            stage.toBack();
            stage.toFront();
            preloaderStage = stage;
        }
        catch (IOException e) {
            throw new PlatformException(e);
        }
    }

    /**
     * Hide the preloader if it is currently visible.
     */
    @Override
    public void hidePreloader() {
        if (preloaderStage != null) {
            preloaderStage.close();
        }
        if (startupProgressListener != null) {
            @SuppressWarnings("unchecked")
            EventBus<StartupProgressEvent> bus = Lookup.lookup(EventBus.class);
            bus.unsubscribe(StartupProgressEvent.class, startupProgressListener);
            startupProgressListener = null;
        }
    }

    /**
     * Preload the module while starting the application.
     * <p>
     * It will be executed in an separate thread while showing the splash screen.
     */
    @Override
    public void preload() {
    }

    /**
     * Request the platform to show the main window.
     *
     * @param stage The stage where the main window will be shown.
     */
    @Override
    public void showMainApplicationWindow(Stage stage) throws PlatformException {
        if (preloaderStage != null && preloaderStage.isShowing()) {
            throw new PlatformException("Can not show main application window while the preloader is visible");
        }
        preloaderStage = null;
        mainApplicationStage = stage;
        WindowManager windowManager = Lookup.lookup(WindowManager.class);
        ApplicationWindow window = Lookup.lookupAll(ApplicationWindow.class).stream()
                .findFirst()
                .orElseThrow(() -> new PlatformException("No ApplicationWindow registered"));
        try {
            window.setStage(stage);
            window.setWindowManager(windowManager);
            window.init();
            stage.show();
            windowManager.init();
        }
        catch (IOException e) {
            throw new PlatformException("Can not initialise application window: " + e.getMessage(), e);
        }
    }

    /**
     * Find the splash screen.
     * It first try to find the application specific splashscreen under {@code /splash/splash.fxml}
     * and if it can not found it uses the default wfx splash screen.
     *
     * @return the url of the found splash screen fxml.
     */
    private URL findSplashScreen() {
        URL url;
        url = getClass().getResource("/splash/splash.fxml");
        if (url == null) {
            return getClass().getResource("/default/splash.fxml");
        }
        else {
            return url;
        }
    }
}
