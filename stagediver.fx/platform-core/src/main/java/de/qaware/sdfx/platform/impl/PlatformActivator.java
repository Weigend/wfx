package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.*;

import static com.sun.javafx.application.LauncherImpl.launchApplication;

/**
 * Activates and deactivates the stagediver.fx platform bundle.
 */
public class PlatformActivator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformActivator.class);

    /**
     * Starts the stagediver.fx platform bundle.
     * <p/>
     * When starting the platform bundle it will start the JavaFX application and show the main window.
     *
     * @param context The bundle context.
     */
    @Override
    public void start(BundleContext context) {
        LOGGER.info("Activate platform core bundle");
        Platform.setImplicitExit(true);
        Thread platformThread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("Launch JavaFX Application");
                launchApplication(PlatformApplication.class, PlatformPreloader.class, new String[]{});
            }
        });
        platformThread.setName("Platform-Application-Thread");
        platformThread.start();
        context.registerService(PreloaderNotificationService.class, new PreloaderNotificationServiceImpl(), null);
    }

    /**
     * Stops the the complete platform.
     * <p/>
     * The platform bundle is a essential part of the stagediver.fx platform so it is required to shutdown the whole
     * osgi platform when this bundle was stoped.
     *
     * @param context The bundle context.
     */
    @Override
    public void stop(BundleContext context) throws BundleException {
        context.getBundle(0).stop();
    }
}
