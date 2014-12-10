// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 20.06.13 17:03
//     description: The platform preloader shows the splash screen during startup.
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.platform.impl;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * This is the preloader implementation for the stagediver.fx platform. It is a generic implementation which can be
 * branded by an application and supports displaying text messages on the splash screen.
 */
public class PlatformPreloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformPreloader.class);
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressText;
    private boolean noLoadingProgress = true;
    private PreloaderNotificationServiceImpl notificationService;

    /**
     * Show the platform preloader within given stage.
     *
     * @param stage the stage where the preloader should shown
     * @throws IOException in case of the preloader fxml can not found.
     */
    public void start(Stage stage) throws IOException {
        LOGGER.info("Starting platform preloader");

        URL splashFxmlUrl = findSplashScreen();

        Parent parent = load(splashFxmlUrl);
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

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

    private Parent load(URL url) throws IOException {
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(this);
        loader.load();
        return loader.getRoot();
    }
}
