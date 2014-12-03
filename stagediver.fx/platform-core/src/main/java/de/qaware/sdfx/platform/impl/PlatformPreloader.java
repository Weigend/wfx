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

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import javafx.application.Platform;
import javafx.application.Preloader;
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
public class PlatformPreloader extends Preloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformPreloader.class);
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressText;
    private Stage stage;
    private boolean noLoadingProgress = true;
    private PreloaderNotificationServiceImpl notificationService;

    @Override
    public void init() {
        PreloaderNotificationService pns = Lookup.lookup(PreloaderNotificationService.class);
        if (pns instanceof PreloaderNotificationServiceImpl) {
            notificationService = (PreloaderNotificationServiceImpl) pns;
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.info("Starting platform preloader");

        URL splashFxmlUrl = findSplashScreen();

        Parent parent = load(splashFxmlUrl);
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        this.stage = stage;
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

    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        LOGGER.debug("Received progress notification: {}", pn);
        if (pn.getProgress() != 1.0 || !noLoadingProgress) {
            progressBar.setProgress(pn.getProgress());
            progressText.setText("Loading Plattform...");
            if (pn.getProgress() > 0) {
                noLoadingProgress = false;
            }
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        //ignore but log, hide after application signals it is ready
        LOGGER.debug("Received state change notification: {}", evt);
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification pn) {
        LOGGER.debug("Received application notification: {}", pn);
        if (pn instanceof ProgressNotification) {
            double v = ((ProgressNotification) pn).getProgress();
            if (pn instanceof de.qaware.sdfx.platform.api.ProgressNotification) {
                progressText.setText(((de.qaware.sdfx.platform.api.ProgressNotification) pn).getMessage());
            }
            progressBar.setProgress(v);
        }
        else if (pn instanceof StateChangeNotification) {
            //hide after get any state update from application
            if (notificationService != null) {
                notificationService.getApplication().showMainStage();
            }
            Platform.runLater(stage::hide);
        }
    }
}
