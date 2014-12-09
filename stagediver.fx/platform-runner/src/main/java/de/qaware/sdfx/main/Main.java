package de.qaware.sdfx.main;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.Module;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private List<Module> modules = new ArrayList<>();

    @Override
    public void init() throws Exception {
        modules = Lookup.lookupAll(Module.class);
    }

    /**
     * Start the application.
     * <p>
     * First it shows within the {@code primaryStage} the preloader and executes parallel the
     * {@link de.qaware.sdfx.platform.api.Module#preload()} method of all modules. After initializing the modules the
     * preloader stage will be closed and it creates the main application window with the window system. Then the
     * {@link de.qaware.sdfx.platform.api.Module#start()} method is executed sequential to finalize the modules startup.
     *
     * @param primaryStage The primary stage which shows the preloader.
     */
    @Override
    public void start(Stage primaryStage) {
        LOGGER.info("Show preloader.");
        // Show Platform Preloader

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
        // show main window
        modules.forEach(Module::start);
    }

    /**
     * Shutdown the application.
     * <p>
     * It first calls the {@link de.qaware.sdfx.platform.api.Module#stop()} method of all modules, close all stages and
     * shutdown the application.
     *
     * @throws java.lang.Exception In case of any erros while stopping the application.
     */
    @Override
    public void stop() throws Exception {
        LOGGER.info("Begin shtudown of platform. Stop modules.");
        modules.forEach(Module::stop);
        super.stop();
        LOGGER.info("Stop application");
    }
}
