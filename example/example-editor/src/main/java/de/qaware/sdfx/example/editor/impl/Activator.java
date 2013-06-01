package de.qaware.sdfx.example.editor.impl;

import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.*;
import java.io.IOException;

/**
 * Register the a example view within the window manager.
 */
public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ServiceTracker<WindowManager, Object> tracker;

    @Override
    public void start(final BundleContext context) throws Exception {
        LOGGER.info("Starting Bundle {}", context.getBundle());
        tracker = new ServiceTracker<>(context, WindowManager.class,
                new ServiceTrackerCustomizer<WindowManager, Object>() {
                    @Override
                    public Object addingService(ServiceReference<WindowManager> reference) {
                        final WindowManager manager = context.getService(reference);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    FXMLView<ExampleController> center =
                                            new FXMLView<>("example-1", "Example GUI", Position.CENTER,
                                                    "/de/qaware/sdfx/example/editor/example.fxml",
                                                    getClass().getClassLoader());

                                    manager.register(center);
                                    //manager.register(explorer, center);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        return manager;
                    }

                    @Override
                    public void modifiedService(ServiceReference<WindowManager> reference, Object o) {
                    }

                    @Override
                    public void removedService(ServiceReference<WindowManager> reference, Object o) {
                    }
                });
        tracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (tracker != null) {
            tracker.close();
        }
    }
}
