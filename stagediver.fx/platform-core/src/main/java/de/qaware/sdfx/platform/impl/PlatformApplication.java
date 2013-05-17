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

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        stage.setTitle("stagediver.fx Platform");
        URL location = getClass().getResource("/de/qaware/sdfx/platform/MainWindow.fxml");

        FXMLLoader fxmlLoader = new FXMLLoader(location);
        Parent parent = (Parent) fxmlLoader.load();
        MainWindowImpl controller = fxmlLoader.getController();

        stage.setScene(new Scene(parent));
        stage.show();

        FrameworkUtil.getBundle(getClass()).getBundleContext()
                .registerService(MainWindow.class, controller, null);

        Thread.currentThread().setContextClassLoader(oldCl);
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
