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

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.windows.DefaultApplicationWindow;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class WindowMtgActivator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowMtgActivator.class);
    private static Lookup lookup = new Lookup(WindowMtgActivator.class);
    private ServiceRegistration registration;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        LOGGER.info("Starting Bundle {}", bundleContext.getBundle());
        lookup.lookup(PreloaderNotificationService.class)
                .sendNotification(bundleContext.getBundle(), "Starting Window Management", 0);

        ApplicationWindow appWindow = lookup.lookup(ApplicationWindow.class);
        if (appWindow == null) {
            bundleContext.registerService(ApplicationWindow.class, new DefaultApplicationWindow(), null);
        }

        lookup.lookup(PreloaderNotificationService.class)
                .sendNotification(bundleContext.getBundle(), "Starting Window Management finished", 1);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
