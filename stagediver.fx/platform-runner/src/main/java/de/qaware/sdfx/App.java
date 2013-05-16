package de.qaware.sdfx;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the  stagediver.fx plattform runner.
 */
public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            LOGGER.info("Starting stagediver.fx platform");
            // Initialize Apache Felix Framework
            Map<String, String> properties = new HashMap<>();
            properties.put(Constants.FRAMEWORK_STORAGE_CLEAN, "onFirstInit");
            properties.put(Constants.FRAMEWORK_BOOTDELEGATION, "javafx.*,com.sun.javafx.*");
            properties.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_APP);
            properties.put("org.osgi.framework.system.packages.extra", "javafx.application, javafx.collections, " +
                    "javafx.event, javafx.fxml, javafx.geometry, javafx.scene, javafx.scene.control, " +
                    "javafx.scene.effect, javafx.scene.input, javafx.scene.layout, javafx.scene.paint, javafx.stage");

            FrameworkFactory factory = new org.apache.felix.framework.FrameworkFactory();
            org.osgi.framework.launch.Framework framework = factory.newFramework(properties);
            framework.init();

            BundleContext context = framework.getBundleContext();
            for (Bundle b : context.getBundles()) {
                try {
                    b.uninstall();
                } catch (Exception e) {
                } // todo: better way to redploy
            }

            // Start and stop framework and bundles
            framework.start();
        } catch (Exception e) {
            LOGGER.error("Can not start stagediver.fx platform", e);
            System.exit(0);
        }
    }
}
