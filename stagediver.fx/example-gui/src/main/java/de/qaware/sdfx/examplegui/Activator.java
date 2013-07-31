package de.qaware.sdfx.examplegui;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.*;
import java.io.IOException;

/**
 * Register the a example view within the window manager.
 */
public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);
    private Lookup lookup = new Lookup(Activator.class);

    @Override
    public void start(final BundleContext context) throws Exception {
        LOGGER.info("Starting Bundle {}", context.getBundle());
        final PreloaderNotificationService notificationService = lookup.lookup(PreloaderNotificationService.class);
        notificationService.sendNotification(context.getBundle(), "Starting example gui", 0);

        final WindowManager manager = lookup.lookup(WindowManager.class);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOGGER.info("Register example view");
                    FXMLView<ExampleController> center =
                            new FXMLView<>("example-1", "Example GUI", Position.CENTER,
                                    "/de/qaware/sdfx/examplegui/example.fxml",
                                    getClass().getClassLoader());

                    FXMLView<ExampleExplorerController> explorer =
                            new FXMLView<>("example-explorer-1", "Example Explorer", Position.LEFT,
                                    "/de/qaware/sdfx/examplegui/example_explorer.fxml",
                                    getClass().getClassLoader());

                    manager.register(center);
                    manager.register(explorer, center);

                    notificationService.sendNotification(context.getBundle(), "Starting example gui", 1);
                    notificationService.sendNotification(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {

    }
}
