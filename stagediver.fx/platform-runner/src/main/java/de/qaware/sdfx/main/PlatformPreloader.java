package de.qaware.sdfx.main;


import org.slf4j.Logger;

import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import java.net.URL;

public class PlatformPreloader extends Preloader{

    private static final Logger LOGGER= org.slf4j.LoggerFactory.getLogger(PlatformPreloader.class);

    @FXML
    ProgressBar progressBar;
    @FXML
    Label progressText;
    Stage stage;
    boolean noLoadingProgress = true;

    @Override
    public void start(Stage stage) throws Exception {
        LOGGER.info("Starting platform preloader");
        URL url = getClass().getResource("/de/qaware/sdfx/splash.fxml");
        FXMLLoader loader = new FXMLLoader(url);
        loader.setController(this);
        loader.load();
        Parent parent = loader.getRoot();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        this.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    @Override
    public void handleProgressNotification(Preloader.ProgressNotification pn) {
        LOGGER.debug("Received progress notification: {}",pn);
        if (pn.getProgress() != 1.0 || !noLoadingProgress) {
            progressBar.setProgress(pn.getProgress() / 2);
            if (pn.getProgress() > 0) {
                noLoadingProgress = false;
            }
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        LOGGER.debug("Received state change notification: {}",evt);
        //ignore, hide after application signals it is ready
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification pn) {
        LOGGER.debug("Received application notification: {}",pn);
        if (pn instanceof Preloader.ProgressNotification) {

            double v = ((Preloader.ProgressNotification) pn).getProgress();
            if (!noLoadingProgress) {
                v = 0.5 + v / 2;
            }
            if (pn instanceof ProgressNotification) {
                progressText.setText(((ProgressNotification) pn).getMessage());
            }

            progressBar.setProgress(v);
        }
        else if (pn instanceof StateChangeNotification) {
            //hide after get any state update from application
            stage.hide();
        }
    }

    public static class ProgressNotification extends Preloader.ProgressNotification {
        private final String message;

        public ProgressNotification(double v) {
            super(v);
            message = null;
        }

        public ProgressNotification(double value, String message) {
            super(value);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
