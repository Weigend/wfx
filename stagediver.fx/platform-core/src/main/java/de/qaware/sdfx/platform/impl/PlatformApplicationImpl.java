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
package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.platform.api.events.ProgressEvent;
import de.qaware.sdfx.platform.api.events.StartupProgressEvent;
import de.qaware.sdfx.platform.api.exceptions.PlatformException;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.stage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * The JavaFX application. It initialize the javafx application thread and the main stage for stagediver.fx platform.
 *
 * @author christian.fritz
 */
@Singleton
@Priority(Integer.MIN_VALUE)
public class PlatformApplicationImpl implements PlatformApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformApplicationImpl.class);
    private Stage mainApplicationStage;
    private Stage preloaderStage;
    private ProgressController progressController;
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
    public void showPreloader(Stage stage) throws PlatformException {
        try {
            URL splashFxmlUrl = findSplashScreen();
            FXMLLoader loader = Lookup.lookup(FXMLLoader.class);
            loader.setLocation(splashFxmlUrl);
            Parent parent = loader.load();
            progressController = loader.getController();
            getEventBus().subscribe(StartupProgressEvent.class, progressController::progress);
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
        if (progressController != null) {
            getEventBus().unsubscribe(ProgressEvent.class, progressController::progress);
            getEventBus().unsubscribe(StartupProgressEvent.class, progressController::progress);
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
        List<ApplicationWindow> windowList = Lookup.lookupAll(ApplicationWindow.class);
        for (ApplicationWindow window : windowList) {
            try {
                window.setStage(stage);
                window.setWindowManager(windowManager);
                window.init();
                stage.show();
                windowManager.init();
                break;
            }
            catch (IOException e) {
                LOGGER.warn("Can not load Application Window", e);
            }
        }
    }

    /**
     * Find the splash screen.
     * It first try to find the application specific splashscreen under {@code /splash/splash.fxml}
     * and if it can not found it uses the default stagediver.fx splash screen.
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
