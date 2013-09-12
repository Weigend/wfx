// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 14.06.13 09:11
//     description: Processor for auto deployment.
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.main;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processor for auto deployment.
 */
public class AutoProcessor {

    /**
     * The property name used for the bundle directory.
     */
    public static final String AUTO_DEPLOY_DIR_PROPERY = "stagediver.auto.deploy.dir";
    /**
     * The default name used for the bundle directory.
     */
    public static final String AUTO_DEPLOY_DIR_VALUE = "bundles";
    /**
     * The property name used to specify auto-deploy start level.
     */
    public static final String AUTO_DEPLOY_STARTLEVEL_PROPERY = "stagediver.auto.deploy.startlevel";
    public static final String AUTO_DEPLOY_BUNDLE_STARTLEVEL = "stagediver.auto.deploy.startlevel.bundle";
    private static StartupLogger logger = new StartupLogger(AutoProcessor.class);
    private BundleContext context;
    private Map<String, String> configProps;
    private List<Bundle> startBundleList = new ArrayList<>();
    private Map<String, Integer> bundelStartLevels = new HashMap<>();
    private int frameworkStartLevel;

    /**
     * Initialize the auto processor.
     *
     * @param context     The bundle context of the framework bundle (bundle id 0).
     * @param configProps The configuration properties which define the auto deploy settings.
     */
    public AutoProcessor(BundleContext context, Map<String, String> configProps) {
        this.context = context;
        this.configProps = configProps;
    }

    protected static boolean isFragment(Bundle bundle) {
        return bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null;
    }

    /**
     * process the auto deployment and installation of bundles.
     *
     * @throws IOException In case of the bundles can not be deployed or started.
     */
    public void process() throws IOException {
        initStartLevels();
        loadBundles();
        startBundles();
    }

    protected void initStartLevels() {
        if (configProps.get(AUTO_DEPLOY_BUNDLE_STARTLEVEL) != null) {
            String[] bundles = configProps.get(AUTO_DEPLOY_BUNDLE_STARTLEVEL).split(" ");
            for (String s : bundles) {
                String[] parts = s.split("@", 2);
                bundelStartLevels.put(parts[0].trim(), Integer.parseInt(parts[1]));
            }
        }
        try {
            frameworkStartLevel = Integer.parseInt(configProps.get(AUTO_DEPLOY_STARTLEVEL_PROPERY));
        } catch (NumberFormatException ex) {
            // Retrieve the Start Level service, since it will be needed
            // to set the start level of the installed bundles.
            FrameworkStartLevel fwStartLevel = context.getBundle(0).adapt(FrameworkStartLevel.class);
            frameworkStartLevel = fwStartLevel.getInitialBundleStartLevel();
        }
        logger.info("Default startlevel for bundles: %s", frameworkStartLevel);
    }

    protected int getStartLevel(Bundle bundle) {

        String bundleIdentifier = bundle.getSymbolicName() + ":" + bundle.getVersion().toString();
        if (bundelStartLevels.containsKey(bundleIdentifier)) {
            return bundelStartLevels.get(bundleIdentifier);
        }
        else if (bundelStartLevels.containsKey(bundle.getSymbolicName())) {
            return bundelStartLevels.get(bundle.getSymbolicName());
        }
        return frameworkStartLevel;
    }

    protected void loadBundles() throws IOException {
        Map<String, Bundle> installedBundleMap = new HashMap<>();
        for (Bundle b : context.getBundles()) {
            installedBundleMap.put(b.getLocation(), b);
        }

        // Get the auto deploy directory.
        String autoDir = configProps.get(AUTO_DEPLOY_DIR_PROPERY);
        autoDir = (autoDir == null) ? AUTO_DEPLOY_DIR_VALUE : autoDir;

        logger.info("Load bundles from %s", autoDir);

        // Look in the specified bundle directory to create a list of all JAR files to install.
        final List<File> jarList = new ArrayList<>();
        Files.walkFileTree(Paths.get(autoDir), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.getFileName().toString().endsWith(".jar")) {
                    jarList.add(file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        // Install bundle JAR files and remember the bundle objects.
        for (File jarFile : jarList) {
            // Look up the bundle by location, removing it from
            // the map of installed bundles so the remaining bundles
            // indicate which bundles may need to be uninstalled.
            Bundle b = installedBundleMap.remove(jarFile.toURI().toString());
            installUpdateBundle(jarFile, b);
        }

        uninstallOldBundles(installedBundleMap);
    }

    protected void uninstallOldBundles(Map<String, Bundle> installedBundleMap) {
        logger.warn("There are bundles in cache they don't should be loaded");
        for (Map.Entry<String, Bundle> entry : installedBundleMap.entrySet()) {
            Bundle b = entry.getValue();
            if (b.getBundleId() != 0) {
                try {
                    b.uninstall();
                } catch (BundleException ex) {
                    logger.error("Auto-deploy uninstall", ex);
                }
            }
        }
    }

    protected void installUpdateBundle(File jarFile, Bundle bundle) {
        try {
            // If the bundle is not already installed, then install it
            Bundle b = bundle;
            if ((b == null)) {
                b = context.installBundle(jarFile.toURI().toString());
            }
            // If the bundle is already installed, then update it
            else {
                b.update();
            }

            // If we have found and/or successfully installed a bundle,
            // then add it to the list of bundles to potentially start
            // and also set its start level accordingly.
            if ((b != null) && !isFragment(b)) {
                startBundleList.add(b);
                BundleStartLevel bundleStartLevel = b.adapt(BundleStartLevel.class);
                bundleStartLevel.setStartLevel(getStartLevel(b));
            }
        } catch (BundleException ex) {
            logger.error("Error during install or update bundle %s for jar file: %s", bundle, jarFile);
            logger.error("Auto-deploy install ", ex);
        }
    }

    protected void startBundles() {
        logger.info("Start bundles");
        for (Bundle b : startBundleList) {
            if (b.getState() != Bundle.ACTIVE) {
                try {
                    b.start();
                } catch (BundleException ex) {
                    logger.error("Auto-deploy start", ex);
                }
            }
        }
    }
}
