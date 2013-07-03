// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 03.06.13 09:54
//     description:
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.platform.api.MainWindow;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import de.qaware.sdfx.windowmtg.api.MainWindowFactory;
import de.qaware.sdfx.windowmtg.windows.MainWindowFactoryImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
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
        bundleContext.getService(bundleContext.getServiceReference(PreloaderNotificationService.class))
                .sendNotification(bundleContext.getBundle(), "Starting Window Management", 0);

       // bundleContext.registerService(MainWindowFactory.class, new MainWindowFactoryImpl(), null);

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
