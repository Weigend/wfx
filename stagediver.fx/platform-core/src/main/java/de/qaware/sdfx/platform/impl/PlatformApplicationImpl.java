// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 04.06.13 21:31
//     description: The JavaFX application for the stagediver.fx platform.
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.platform.api.exceptions.PlatformException;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * The JavaFX application. It initialize the javafx application thread and the main stage for stagediver.fx platform.
 *
 * @author christian.fritz
 */
@Singleton
public class PlatformApplicationImpl implements PlatformApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformApplicationImpl.class);
    private Stage mainApplicationStage;
    private Stage preloaderStage;

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
        return "";
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
     */
    @Override
    public void showPreloader(Stage stage) throws IOException {
        URL splashFxmlUrl = findSplashScreen();
        Parent parent = FXMLLoader.load(splashFxmlUrl);
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
        preloaderStage = stage;
    }

    /**
     * Hide the preloader if it is currently visible.
     */
    @Override
    public void hidePreloader() {
        if (preloaderStage != null) {
            preloaderStage.close();
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
                LOGGER.debug("Can not load Application Window", e);
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
