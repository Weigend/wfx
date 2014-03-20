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
import java.util.*;

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

    protected int getFrameworkStartLevel() {
        return frameworkStartLevel;
    }

    protected Map<String, Integer> getBundelStartLevels() {
        return bundelStartLevels;
    }

    protected List<Bundle> getStartBundleList() {
        return startBundleList;
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

    /**
     * Initialize the framework and bundle startlevels.
     */
    public void initStartLevels() {
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

    /**
     * Get the startlevel for a given bundle.
     *
     * @param bundle The bundle.
     * @return The startlevel for the given bundle.
     */
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

    /**
     * Load the bundles.
     *
     * @throws IOException In case of a bundle can not be loaded.
     */
    protected void loadBundles() throws IOException {
        // Get the auto deploy directory.
        String autoDir = configProps.get(AUTO_DEPLOY_DIR_PROPERY);
        autoDir = (autoDir == null) ? AUTO_DEPLOY_DIR_VALUE : autoDir;
        autoDir = absoluteBundleDir(autoDir);

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

        installBundles(jarList);
    }

    /**
     * Install the items of the given list of jars as bundles.
     *
     * @param jarList The jars to install.
     */
    public void installBundles(Collection<File> jarList) {
        Map<String, Bundle> installedBundleMap = new HashMap<>();
        for (Bundle b : context.getBundles()) {
            installedBundleMap.put(b.getLocation(), b);
        }

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

    /**
     * Get the absolute path of the bundle directory.
     * <p/>
     * It try also to find the bundle directory if it was started from an ide (like intellij).
     *
     * @param bundleDir The bundle dir path directly form the config.
     * @return The full path to the bundle directory.
     */
    protected static String absoluteBundleDir(String bundleDir) {
        File dir = new File(bundleDir);
        File targetDir = new File("target" + File.separator + bundleDir);
        if (dir.isAbsolute()) {
            return bundleDir;
        }
        else if (targetDir.exists()) {
            return targetDir.getAbsolutePath();
        }
        else {
            return dir.getAbsolutePath();
        }
    }

    /**
     * Uninstall all bundles thats were installed but shut not activated while the next start.
     *
     * @param installedBundleMap A map with all previous installed bundles.
     */
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

    /**
     * Install or update the given jar bundle combination.
     * <p/>
     * If {@param bundle} is null than the bundle will be installed otherwise the bundle is updated.
     * In the case of updateing the bundle the bundle id will be the same.
     *
     * @param jarFile The jar from where the bundle should be installed
     * @param bundle  The previous installed bundle instance.
     */
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
                int startLevel = getStartLevel(b);
                logger.debug("Set startlevel " + startLevel + " for bundle " + b);
                bundleStartLevel.setStartLevel(startLevel);
            }
        } catch (BundleException ex) {
            logger.error("Error during install or update bundle %s for jar file: %s", bundle, jarFile);
            logger.error("Auto-deploy install ", ex);
        }
    }

    /**
     * Start all bundles from the internal bundle start list.
     */
    public void startBundles() {
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
