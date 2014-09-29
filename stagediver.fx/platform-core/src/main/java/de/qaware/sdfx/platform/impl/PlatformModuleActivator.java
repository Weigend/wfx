// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 04.06.13 21:50
//     description: Activates the the stagediver.fx platform.
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.platform.api.ModuleActivator;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.sun.javafx.application.LauncherImpl.launchApplication;

/**
 * Activates and deactivates the stagediver.fx platform bundle.
 */
public class PlatformModuleActivator implements ModuleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformModuleActivator.class);

    /**
     * Starts the stagediver.fx platform bundle.
     * <p/>
     * When starting the platform bundle it will start the JavaFX application and show the main window.
     */
    @Override
    public void start() {
        LOGGER.info("Activate platform core bundle");
        Platform.setImplicitExit(true);
        Thread platformThread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("Launch JavaFX Application");
                launchApplication(PlatformApplicationImpl.class, PlatformPreloader.class, new String[]{});
            }
        });
        platformThread.setName("Platform-Application-Thread");
        platformThread.start();
    }

    @Override
    public void stop() {
    }

    @Override
    public List<Class<? extends ModuleActivator>> getDependsOnStart() {
        return null;
    }

    @Override
    public List<Class<? extends ModuleActivator>> dependsOnStop() {
        return null;
    }
}
