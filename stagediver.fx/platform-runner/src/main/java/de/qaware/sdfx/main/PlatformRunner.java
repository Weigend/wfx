package de.qaware.sdfx.main;

import com.google.inject.Inject;
import de.qaware.sdfx.platform.api.ModuleActivator;

import java.util.List;

/**
 * @author christian.fritz
 */
public class PlatformRunner {
    @Inject
    private List<ModuleActivator> activators;

    void run() {
        // TODO: Respect the depends on start declarations
        for (ModuleActivator activator : activators) {
            activator.start();
        }
    }
}
