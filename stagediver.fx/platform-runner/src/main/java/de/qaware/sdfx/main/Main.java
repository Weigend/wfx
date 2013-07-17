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
    public static final String CONFIG_DIRECTORY = "conf";
    private static Logger logger = new Logger(Main.class);
    private Framework framework;
    private Map<String, String> configProps;

    /**
     * The initial main method to start up the platform.
     *
     * @param args The commandline arguments
     * @throws Exception In any case of problems which can not be handled by the platform.
     */
    public static void main(String[] args) throws Exception {
        new Main().run();
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
    protected static Map<String, String> loadProperties(String propertiesProp, String defaultPropertiesFile) {
        // The config properties file is either specified by a system
        // property or it is in the conf/ directory of the Felix
        // installation directory.  Try to load it from one of these
        // places.

        // See if the property URL was specified as a property.
        logger.info("Loading properties file");
        URL propURL;
        String custom = System.getProperty(propertiesProp);
        if (custom != null) {
            try {
                propURL = new URL(custom);
            } catch (MalformedURLException ex) {
                logger.error("Malformed URL given for loading properties", ex);
                return null;
            }
        }
        else if (Main.class.getResource(defaultPropertiesFile) != null) {
            propURL = Main.class.getResource(defaultPropertiesFile);
        }
        else if (Main.class.getResource("/" + defaultPropertiesFile) != null) {
            propURL = Main.class.getResource("/" + defaultPropertiesFile);
        }
        else {
            // Determine where the configuration directory is by figuring
            // out where felix.jar is located on the system class path.
            File confDir;
            String classpath = System.getProperty("java.class.path");
            int index = classpath.toLowerCase().indexOf("felix.jar");
            int start = classpath.lastIndexOf(File.pathSeparator, index) + 1;
            if (index >= start) {
                // Get the path of the felix.jar file.
                String jarLocation = classpath.substring(start, index);
                // Calculate the conf directory based on the parent
                // directory of the felix.jar directory.
                confDir = new File(new File(new File(jarLocation).getAbsolutePath()).getParent(), CONFIG_DIRECTORY);
            }
            else {
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

    protected void initFramework() throws Exception {
        logger.info("Init the framework");
        FrameworkFactory factory = getFrameworkFactory();
        logger.debug("Using framework factory: %s", factory);
        framework = factory.newFramework(configProps);
        getFramework().init();
    }

    protected void addShutdownHook() {
        // If enabled, register a shutdown hook to make sure the framework is
        // cleanly shutdown when the VM exits.
        String enableHook = configProps.get(SHUTDOWN_HOOK_PROP);
        if ((enableHook == null) || !enableHook.equalsIgnoreCase("false")) {
            logger.debug("Add shutdown hook");
            Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {
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
            });
        }
    }

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

    protected FrameworkFactory getFrameworkFactory() throws Exception {
        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class);
        return loader.iterator().next();
    }

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
