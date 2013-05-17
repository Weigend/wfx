package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.platform.api.MainWindow;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.stage.*;
import java.io.IOException;
import java.net.URL;

/**
 * The JavaFX application. It initialize the javafx application thread and the main stage for stagediver.fx platform.
 */
public class PlatformApplication extends Application {

    /**
     * Start the JavaFX application. This will include the initialization of the content for the first stage and
     * show the primary window for the stagediver.fx platform.
     *
     * @param stage The primary window stage.
     * @throws IOException In case of any fxml loading failure.
     */
    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("stagediver.fx Platform");
        URL location = getClass().getResource("/de/qaware/sdfx/platform/MainWindow.fxml");

        FXMLLoader loader = new FXMLLoader(location);
        loader.setClassLoader(getClass().getClassLoader());
        Parent parent = (Parent) loader.load();
        MainWindowImpl controller = loader.getController();
        controller.setStage(stage);
        stage.setScene(new Scene(parent));
        stage.show();

        FrameworkUtil.getBundle(getClass()).getBundleContext()
                .registerService(MainWindow.class, controller, null);
    }

    /**
     * Shutdown the JavaFX application and stop the platform bundle.
     *
     * @throws BundleException
     */
    @Override
    public void stop() throws BundleException {
        FrameworkUtil.getBundle(getClass()).stop();
    }
}
