package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.platform.api.MainWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class WindowMtgActivator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowMtgActivator.class);

    private ServiceRegistration registration;

    private ServiceTracker<MainWindow, Object> tracker;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        LOGGER.info("Starting Bundle {}", bundleContext.getBundle());

        tracker = new ServiceTracker<>(bundleContext, MainWindow.class,
                new ServiceTrackerCustomizer<MainWindow, Object>() {

                    @Override
                    public Object addingService(ServiceReference<MainWindow> serviceReference) {
                        LOGGER.info("Init WindowManager");
                        MultiWindowManager manager = new WindowManagerImpl();

                        MainWindow controller = bundleContext.getService(serviceReference);
                        controller.setWindowManager(manager);

                        registration = bundleContext.registerService(WindowManager.class, manager, null);
                        return controller;
                    }

                    @Override
                    public void modifiedService(ServiceReference serviceReference, Object o) {
                    }

                    @Override
                    public void removedService(ServiceReference serviceReference, Object o) {
                        registration.unregister();
                    }
                }
        );
        tracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        if (tracker != null) {
            tracker.close();
        }
    }
}
