package de.qaware.sdfx.platform.impl;

import javafx.application.Application;
import javafx.application.Platform;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class PlatformActivator implements BundleActivator {

    @Override
    public void start(BundleContext context) throws Exception {
        Platform.setImplicitExit(true);
        Thread platformThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Application.launch(PlatformApplication.class, null);
            }
        });
        platformThread.setName("Platform-Application-Thread");
        platformThread.start();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        context.getBundle(0).stop();
    }
}
