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

import de.qaware.sdfx.platform.api.PreloaderNotificationService;
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

    private ServiceRegistration registration;

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        LOGGER.info("Starting Bundle {}", bundleContext.getBundle());
        bundleContext.getService(bundleContext.getServiceReference(PreloaderNotificationService.class))
                .sendNotification(bundleContext.getBundle(), "Starting Window Management", 0);

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
