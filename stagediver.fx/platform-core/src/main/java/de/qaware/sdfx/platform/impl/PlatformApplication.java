package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.platform.api.MainWindow;
import org.osgi.framework.FrameworkUtil;

import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.stage.*;
import java.net.URL;

/**
 *
 */
public class PlatformApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        stage.setTitle("Hello World!");
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

    @Override
    public void stop() throws Exception {
        FrameworkUtil.getBundle(getClass()).stop();
    }
}
