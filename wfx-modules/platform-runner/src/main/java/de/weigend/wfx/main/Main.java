/*
 * #%L
 * The platform-runner module is main start module for the wfx platform.
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
package de.weigend.wfx.main;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.lookup.impl.ServiceLoaderLookupStrategy;
import de.weigend.wfx.lookup.impl.ServiceLoaderLookupStrategy.Producer;
import de.weigend.wfx.platform.api.Module;
import de.weigend.wfx.platform.api.PlatformApplication;
import de.weigend.wfx.platform.api.exceptions.PlatformException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * wfx application startup class. It controls the full application lifecycle beginning with showing the
 * preloader over bootstrapping the modules, showing the main application window and shutdown the application inclusive
 * all modules.
 *
 * @author Software-EKG Team
 */
public class Main extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private List<Module> modules = new ArrayList<>();
    private PlatformApplication platformApplication;

    @Override
    public void init() {
        Thread.setDefaultUncaughtExceptionHandler((t, e) ->
                LOGGER.error("Uncaught Exception in thread '" + t.getName() + "': ", e));

        if (Lookup.getLookupStrategy() == null) {
            ServiceLoaderLookupStrategy lookupStrategy = new ServiceLoaderLookupStrategy();
            lookupStrategy.init(FXMLLoader.class, (Producer<FXMLLoader>) FXMLLoader::new);
            Lookup.init(lookupStrategy);
        }
        modules = Lookup.lookupAll(Module.class);
        platformApplication = Lookup.lookup(PlatformApplication.class);
    }

    /**
     * Start the application.
     * <p>
     * First it shows within the {@code primaryStage} the preloader and executes parallel the
     * {@link de.weigend.wfx.platform.api.Module#preload()} method of all modules. After initializing the modules the
     * preloader stage will be closed and it creates the main application window with the window system. Then the
     * {@link de.weigend.wfx.platform.api.Module#start()} method is executed sequential to finalize the modules startup.
     *
     * @param primaryStage The primary stage which shows the preloader.
     * @throws de.weigend.wfx.platform.api.exceptions.PlatformException In case of the preloader was not closed before
     *                                                                  the application window should be shown.
     */
    @Override
    public void start(Stage primaryStage) throws PlatformException {
        LOGGER.info("Show preloader");
        platformApplication.preload();
        try {
            Stage preloaderStage = new Stage();
            preloaderStage.initOwner(primaryStage);
            platformApplication.showPreloader(preloaderStage);
            new Thread(() -> {
                startupModules();
                Platform.runLater(() -> finishStartup(primaryStage));
            }, "Background Startup").start();
        }
        catch (Exception e) {
            LOGGER.warn("Can not show preloader!", e);
        }
    }

    /**
     * Startup all modules.
     */
    private void startupModules() {
        modules.parallelStream().forEach((module) -> {
            try {
                LOGGER.info("Startup module {}:{}", module.getName(), module.getVersion());
                module.preload();
                LOGGER.debug("Finished startup of module {}:{}", module.getName(), module.getVersion());
            }
            catch (PlatformException e) {
                LOGGER.warn("Can not start module: " + module.getName(), e);
            }
        });
    }

    /**
     * Finish the startup.
     *
     * @param primaryStage The primary stage where the main window should be shown.
     */
    private void finishStartup(Stage primaryStage) {
        try {
            LOGGER.info("Hide preloader and show main window.");
            platformApplication.hidePreloader();
            platformApplication.showMainApplicationWindow(primaryStage);
            platformApplication.start();
            modules.forEach(Module::start);
        }
        catch (PlatformException e) {
            LOGGER.warn("Can not start application", e);
            Platform.exit();
        }
    }

    /**
     * Shutdown the application.
     * <p>
     * It first calls the {@link de.weigend.wfx.platform.api.Module#stop()} method of all modules, close all stages and
     * shutdown the application.
     *
     * @throws java.lang.Exception In case of any erros while stopping the application.
     */
    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void stop() throws Exception {
        LOGGER.info("Begin shutdown of platform. Stop modules.");
        modules.forEach(Module::stop);
        platformApplication.stop();
        super.stop();
        LOGGER.info("Stop application");
    }
}
