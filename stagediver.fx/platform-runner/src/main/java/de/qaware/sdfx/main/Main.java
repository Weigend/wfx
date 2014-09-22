// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 14.06.13 13:23
//     description: This is the  stagediver.fx platform runner.
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.main;

import org.apache.felix.framework.util.Util;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * This is the  stagediver.fx platform runner.
 */
@Deprecated
public class Main {

    /**
     * The property name used to specify whether the launcher should
     * install a shutdown hook.
     */
    public static final String SHUTDOWN_HOOK_PROP = "stagediver.shutdown.hook";
    /**
     * The property name used to specify an URL to the system
     * property file.
     */
    public static final String SYSTEM_PROPERTIES_PROP = "stagediver.system.properties";
    /**
     * The default name used for the system properties file.
     */
    public static final String SYSTEM_PROPERTIES_FILE_VALUE = "system.properties";
    /**
     * The property name used to specify an URL to the configuration
     * property file to be used for the created the framework instance.
     */
    public static final String CONFIG_PROPERTIES_PROP = "stagediver.config.properties";
    /**
     * The default name used for the configuration properties file.
     */
    public static final String CONFIG_PROPERTIES_FILE_VALUE = "config.properties";
    /**
     * Name of the configuration directory.
     */
    public static final String CONFIG_DIRECTORY = "config";
    private static StartupLogger logger = new StartupLogger(Main.class);
    protected Map<String, String> configProps;
    protected Thread shutdownThread;
    private Framework framework;

    /**
     * The initial main method to start up the platform.
     *
     * @param args The commandline arguments
     */
    public static void main(String[] args) {
        new Main().run();
    }

    /**
     * Load the porperties from a given file.
     *
     * @param propertiesProp        The system propery where the filename can be found.
     * @param defaultPropertiesFile The default file name.
     * @return A property map of the loaded file.
     */
    protected static Map<String, String> loadProperties(String propertiesProp, String defaultPropertiesFile) {
        URL propURL = getPropertyFileUrl(propertiesProp, defaultPropertiesFile);
        if (propURL == null) {
            return null;
        }
        return loadProperties(propURL);
    }

    /**
     * <p>
     * Loads the configuration properties in the configuration property file
     * associated with the framework installation; these properties
     * are accessible to the framework and to bundles and are intended
     * for configuration purposes. By default, the configuration property
     * file is located in the <tt>conf/</tt> directory of the Felix
     * installation directory and is called "<tt>config.properties</tt>".
     * The installation directory of Felix is assumed to be the parent
     * directory of the <tt>felix.jar</tt> file as found on the system class
     * path property. The precise file from which to load configuration
     * properties can be set by initializing the "<tt>felix.config.properties</tt>"
     * system property to an arbitrary URL.
     * </p>
     *
     * @return A <tt>Properties</tt> instance or <tt>null</tt> if there was an error.
     */
    protected static Map<String, String> loadProperties(URL propURL) {

        logger.info("Loading properties file");

        // Read the properties file.
        Properties props = new Properties();
        logger.debug("Loading properties from url %s", propURL);
        try (InputStream is = propURL.openConnection().getInputStream()) {
            props.load(is);
        } catch (IOException ex) {
            logger.debug("Can not load properties", ex);
            return null;
        }

        // Perform variable substitution for system properties and
        // convert to dictionary.
        Map<String, String> map = new HashMap<>();
        for (Map.Entry entry : props.entrySet()) {
            String name = (String) entry.getKey();
            map.put(name, Util.substVars((String) entry.getValue(), name, null, props));
        }
        return map;
    }

    /**
     * Get the url of a property file.
     *
     * @param propertiesProp        The systme property name where the property file name can be found
     * @param defaultPropertiesFile The default file name if the file from "propertiesProp" can not
     *                              be found or is empty.
     * @return Return the full qualified url the searched property file.
     */
    protected static URL getPropertyFileUrl(String propertiesProp, String defaultPropertiesFile) {
        URL propURL;
        String custom = System.getProperty(propertiesProp);
        if (custom != null) {
            try {
                propURL = new URL(custom);
            } catch (MalformedURLException ex) {
                logger.error("Malformed URL given for loading properties", ex);
                return null;
            }
        } else if (Main.class.getResource(defaultPropertiesFile) != null) {
            propURL = Main.class.getResource(defaultPropertiesFile);
        } else if (Main.class.getResource("/" + defaultPropertiesFile) != null) {
            propURL = Main.class.getResource("/" + defaultPropertiesFile);
        } else {
            File jarLocation = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            if (jarLocation.toString().endsWith(".jar")) {
                jarLocation = jarLocation.getParentFile();
            }

            File confDir = new File(jarLocation, CONFIG_DIRECTORY);
            logger.debug("confDir: %s", confDir);
            if (!confDir.exists()) {
                // Can't figure it out so use the current directory as default.
                confDir = new File(System.getProperty("user.dir"), CONFIG_DIRECTORY);
            }

            try {
                propURL = new File(confDir, defaultPropertiesFile).toURI().toURL();
            } catch (MalformedURLException ex) {
                logger.error("Malformed URL given for loading properties", ex);
                return null;
            }
        }
        return propURL;
    }

    /**
     * Run the platform.
     */
    public void run() {
        loadProperties();
        addShutdownHook();
        try {
            initFramework();
            new AutoProcessor(getFramework().getBundleContext(), configProps).process();
            runFramework();
        } catch (Exception ex) {
            logger.error("Could not create framework", ex);
        }
    }

    /**
     * Run the initialized framework until it stops.
     *
     * @throws BundleException      In case of any bundle failures.
     * @throws InterruptedException In case of the thread is interuppted unexpected.
     */
    protected void runFramework() throws BundleException, InterruptedException {
        FrameworkEvent event;
        do {
            logger.info("Start the framework.");
            getFramework().start();
            // Wait for framework to stop to exit the VM.
            event = getFramework().waitForStop(0);
        }
        // If the framework was updated, then restart it.
        while (event.getType() == FrameworkEvent.STOPPED_UPDATE);
        logger.info("Framework stopped");
    }

    /**
     * Initialize the osgi framework.
     *
     * @throws BundleException In case of the framework can not be initialized.
     */
    protected void initFramework() throws BundleException {
        logger.info("Init the framework");
        FrameworkFactory factory = getFrameworkFactory();
        logger.debug("Using framework factory: %s", factory);
        framework = factory.newFramework(configProps);
        getFramework().init();
    }

    /**
     * Add the osgi framework shutdown hook.
     * <p/>
     * The shutdown hook will shutdown all active bundles and stop the framework when the jvm is requested to stop.
     */
    protected void addShutdownHook() {
        // If enabled, register a shutdown hook to make sure the framework is
        // cleanly shutdown when the VM exits.
        String enableHook = configProps.get(SHUTDOWN_HOOK_PROP);
        if (!"false".equalsIgnoreCase(enableHook)) {
            logger.debug("Add shutdown hook");
            shutdownThread = new Thread("Felix Shutdown Hook") {
                /**
                 * Stop the framework on jvm shutdown.
                 */
                public void run() {
                    try {
                        if (getFramework() != null) {
                            getFramework().stop();
                            getFramework().waitForStop(0);
                        }
                    } catch (Exception ex) {
                        logger.error("Error stopping framework", ex);
                    }
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }
    }

    /**
     * Load all property files that are needed to startup the framework successfully.
     */
    protected void loadProperties() {
        Map<String, String> systemProps = loadProperties(SYSTEM_PROPERTIES_PROP, SYSTEM_PROPERTIES_FILE_VALUE);
        if (systemProps != null) {
            System.getProperties().putAll(systemProps);
        }

        configProps = loadProperties(CONFIG_PROPERTIES_PROP, CONFIG_PROPERTIES_FILE_VALUE);
        if (configProps == null) {
            logger.warn("No %s found.", CONFIG_PROPERTIES_FILE_VALUE);
            configProps = new HashMap<>();
        }
        copySystemProperties();
    }

    /**
     * Load the framework factory to initialize the osgi container.
     * <p/>
     * It will choose the first factory within the classpath that are named within
     * "services/org.osgi.framework.launch.FrameworkFactory".
     * For more information see {@link java.util.ServiceLoader#load(Class)}.
     *
     * @return The fully instanciate factory to initialize the osgi container.
     */
    public FrameworkFactory getFrameworkFactory() {
        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class);
        return loader.iterator().next();
    }

    /**
     * Copy all framework related system properties into the framework initialize properties.
     */
    protected void copySystemProperties() {
        for (Enumeration e = System.getProperties().propertyNames();
             e.hasMoreElements(); ) {
            String key = (String) e.nextElement();
            if (key.startsWith("felix.") || key.startsWith("org.osgi.framework.") || key.startsWith("stagediver.")) {
                configProps.put(key, System.getProperty(key));
            }
        }
    }

    protected Framework getFramework() {
        return framework;
    }
}
