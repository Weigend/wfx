// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 13.12.13 12:56
//      description: Run the current project within the osgi container
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.maven.plugin.run;

import de.qaware.sdfx.main.AutoProcessor;
import de.qaware.sdfx.main.Main;
import de.qaware.sdfx.main.StartupLogger;
import de.qaware.sdfx.maven.plugin.AbstractBundleResolverMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The stagediver.fx run goal.
 * <p/>
 * The goal "run" is a wrapper to start a single module or the full project within the OSGi Container. Which part of the
 * full application is started depends on the selected module from where you start the plugin. It always starts current
 * selected module (current working directory) inclusive all dependencies and the dependencies of the stagediver.fx
 * platform module (de.qaware.stagediver.fx:platform-full).
 *
 * @author christian.fritz
 */
@Mojo(
        name = "run",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresProject = true
)
public class RunMojo extends AbstractBundleResolverMojo {
    /**
     * The default properties which are essentially needed for starting the stagediver.fx within the osgi framework.
     */
    protected static final Map<String, String> DEFAULT_PROPS = new HashMap<String, String>() {
        {
            put("org.osgi.framework.system.packages.extra", "javafx.animation, javafx.application, javafx.beans, " +
                    "javafx.beans.binding, javafx.beans.property, javafx.beans.property.adapter, javafx.beans.value," +
                    "javafx.collections, javafx.collections.transformation, javafx.concurrent, javafx.css, " +
                    "javafx.embed.swing, javafx.embed.swt, javafx.event, javafx.fxml, javafx.geometry, javafx.print, " +
                    "javafx.scene, javafx.scene.canvas, javafx.scene.chart, javafx.scene.control, " +
                    "javafx.scene.control.cell, javafx.scene.effect, javafx.scene.image, javafx.scene.input, " +
                    "javafx.scene.layout, javafx.scene.media, javafx.scene.paint, javafx.scene.shape, " +
                    "javafx.scene.text, javafx.scene.transform, javafx.scene.web, javafx.stage, javafx.util, " +
                    "javafx.util.converter, netscape.javascript, com.sun.javafx.application");
            put("org.osgi.framework.bundle.parent", "app");
            put("org.osgi.framework.storage.clean", "onFirstInit");
            put("org.osgi.framework.bootdelegation", "javafx.*,com.sun.javafx.*");
            put("org.osgi.framework.startlevel.beginning", "5");
            put("felix.startlevel.bundle", "5");
            put("felix.log.level", "1");
            put("AUTO_DEPLOY_STARTLEVEL_PROPERY", "5");
            put("stagediver.auto.deploy.startlevel.bundle", "de.qaware.stagediver.fx.platform-core@2 org.apache.felix" +
                    ".gogo.shell@1 org.apache.felix.gogo.command@1 org.apache.felix.gogo.runtime@1 org.ops4j.pax" +
                    ".logging.pax-logging-service@1 de.qaware.stagediver.fx.windowmanager-core@2 de.qaware.stagediver" +
                    ".fx.example-gui@3 org.apache.felix.scr@1 de.qaware.stagediver.fx.platform-api@1 de.qaware" +
                    ".stagediver.fx.windowmanager-api@1");
        }
    };

    /**
     * Additional configuration properties from the pom.
     * <p/>
     * The can override the default properties.
     */
    @Parameter(alias = "osgiProperties")
    protected Map<String, String> configProps;

    /**
     * The osgi framework instance.
     */
    protected Framework framework;

    /**
     * The stagediver.fx default runner.
     */
    protected Main defaultRunner = new Main();

    /**
     * Executes the run mojo.
     *
     * @throws MojoExecutionException In case the required artifacts can not be resolved or the framework can not be started.
     */
    @Override
    public void execute() throws MojoExecutionException {
        StartupLogger.Level.DEBUG.setEnabled(getLog().isDebugEnabled());
        System.setProperty("binary.css", "false");
        initFramework();
        AutoProcessor bundleProcessor = new AutoProcessor(framework.getBundleContext(), mergeProperties());
        bundleProcessor.initStartLevels();
        bundleProcessor.installBundles(getBundles());
        bundleProcessor.startBundles();
        runFramework();
    }

    /**
     * Start and wait for stop of the osgi framework.
     *
     * @throws MojoExecutionException In case of the starting of framework is not possible.
     */
    protected void runFramework() throws MojoExecutionException {
        try {
            FrameworkEvent event;
            do {
                getLog().info("Start the framework.");
                framework.start();
                // Wait for framework to stop to exit the VM.
                event = framework.waitForStop(0);
            }
            // If the framework was updated, then restart it.
            while (event.getType() == FrameworkEvent.STOPPED_UPDATE);
            getLog().info("Framework stopped");
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Unexpected interrupt while executing stagediver.fx", e);
        } catch (BundleException e) {
            throw new MojoExecutionException("Start of stagediver.fx framework was not possible", e);
        }
    }

    /**
     * Initialize the osgi framework.
     *
     * @throws MojoExecutionException In case of the initialisation of the osgi framework failed.
     */
    protected void initFramework() throws MojoExecutionException {
        try {
            getLog().info("Init the framework");
            FrameworkFactory factory = defaultRunner.getFrameworkFactory();
            getLog().debug("Using framework factory: " + factory);
            framework = factory.newFramework(mergeProperties());
            initShutdownHook();
            framework.init();
        } catch (BundleException e) {
            throw new MojoExecutionException("Initialisation of osgi framework failed", e);
        }
    }

    /**
     * Merge the configurated properties from the pom with the default properties.
     *
     * @return The merged properties map.
     */
    protected Map<String, String> mergeProperties() {
        if (configProps == null) {
            return DEFAULT_PROPS;
        }
        Map<String, String> props = new HashMap<>(DEFAULT_PROPS);
        for (Map.Entry<String, String> entry : configProps.entrySet()) {
            props.put(entry.getKey(), entry.getValue());
        }
        return props;
    }

    private void initShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {
            public void run() {
                try {
                    if (framework != null) {
                        framework.stop();
                        framework.waitForStop(0);
                    }
                } catch (Exception ex) {
                    getLog().error("Error stopping framework", ex);
                }
            }
        });
    }
}
