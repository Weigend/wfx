package de.qaware.sdfx.main;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy;
import de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy.Producer;
import de.qaware.sdfx.platform.api.Module;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.platform.api.exceptions.PlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.*;
import javafx.fxml.*;
import javafx.stage.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * stagediver.fx application startup class. It controls the full application lifecycle beginning with showing the
 * preloader over bootstrapping the modules, showing the main application window and shutdown the application inclusive
 * all modules.
 *
 * @author christian.fritz
 */
public class Main extends Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private List<Module> modules = new ArrayList<>();
    private PlatformApplication platformApplication;

    @Override
    public void init() throws Exception {
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
     * <p/>
     * First it shows within the {@code primaryStage} the preloader and executes parallel the
     * {@link de.qaware.sdfx.platform.api.Module#preload()} method of all modules. After initializing the modules the
     * preloader stage will be closed and it creates the main application window with the window system. Then the
     * {@link de.qaware.sdfx.platform.api.Module#start()} method is executed sequential to finalize the modules startup.
     *
     * @param primaryStage The primary stage which shows the preloader.
     * @throws de.qaware.sdfx.platform.api.exceptions.PlatformException In case of the preloader was not closed before
     *                                                                  the application window should be shown.
     */
    @Override
    public void start(Stage primaryStage) throws PlatformException, IOException {
        LOGGER.info("Show preloader");

        platformApplication.preload();
        platformApplication.showPreloader(primaryStage);

        modules.parallelStream().forEach((module) -> {
            try {
                LOGGER.info("Startup module {}:{}", module.getName(), module.getVersion());
                module.preload();
                LOGGER.debug("Finished startup of module {}:{}", module.getName(), module.getVersion());
            }
            catch (Exception e) {
                LOGGER.warn("Can not start module: " + module.getName(), e);
            }
        });

        LOGGER.info("Hide preloader and show main window.");
        platformApplication.hidePreloader();
        platformApplication.showMainApplicationWindow(new Stage());
        platformApplication.start();
        modules.forEach(Module::start);
    }

    /**
     * Shutdown the application.
     * <p/>
     * It first calls the {@link de.qaware.sdfx.platform.api.Module#stop()} method of all modules, close all stages and
     * shutdown the application.
     *
     * @throws java.lang.Exception In case of any erros while stopping the application.
     */
    @Override
    public void stop() throws Exception {
        LOGGER.info("Begin shtudown of platform. Stop modules.");
        modules.forEach(Module::stop);
        platformApplication.stop();
        super.stop();
        LOGGER.info("Stop application");
    }
}
