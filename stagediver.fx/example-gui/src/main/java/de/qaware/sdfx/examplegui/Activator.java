package de.qaware.sdfx.examplegui;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.*;
import java.io.IOException;

/**
 * Register the a example view within the window manager.
 */
public class Activator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);
    private Lookup lookup = new Lookup(Activator.class);

    public void start() {
        //LOGGER.info("Starting Bundle {}", context.getBundle());
        final PreloaderNotificationService notificationService = lookup.lookup(PreloaderNotificationService.class);
        //notificationService.sendNotification(context.getBundle(), "Starting example gui", 0);

        final WindowManager manager = lookup.lookup(WindowManager.class);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ClassLoader classLoader = getClass().getClassLoader();
                    LOGGER.info("Register example view");
                    FXMLView<ExampleController> center =
                            new FXMLView<>("example-1", "Example GUI", Position.CENTER,
                                    "de/qaware/sdfx/examplegui/example.fxml", 0.7,
                                    classLoader);

                    FXMLView<ExampleExplorerController> explorer =
                            new FXMLView<>("example-explorer-1", "Example Explorer", Position.LEFT,
                                    "de/qaware/sdfx/examplegui/example_explorer.fxml", 0.3,
                                    classLoader);

                    manager.register(center);
                    manager.register(explorer, center);

                    //notificationService.sendNotification(context.getBundle(), "Starting example gui", 1);
                    notificationService.sendNotification(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));

                }
                catch (IOException e) {
                    LOGGER.error("Can not start module", e);
                }
            }
        });
    }

}
