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

package de.qaware.sdfx.maven.plugin;

import de.qaware.sdfx.main.AutoProcessor;
import de.qaware.sdfx.main.Main;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Mojo(
        name = "run",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresProject = true
)
public class RunMojo extends AbstractMojo {

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
    private static final String BUNDLE_EXTESION = ".jar";

    @Parameter(alias = "osgiProperties")
    protected Map<String, String> configProps;

    /**
     * The Maven project.
     */
    @Component
    protected MavenProject project;

    protected Framework framework;

    protected Main defaultRunner = new Main();

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     */
    @Component
    protected RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     */
    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution.
     */
    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    protected List<RemoteRepository> remoteRepos;

    protected ArtifactResolver artifactResolver;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            System.setProperty("binary.css", "false");
            initFramework();
            AutoProcessor bundleProcessor = new AutoProcessor(framework.getBundleContext(), mergeProperties());
            bundleProcessor.initStartLevels();
            bundleProcessor.installBundles(getBundles());
            bundleProcessor.startBundles();
            runFramework();
        } catch (Exception e) {
            throw new MojoExecutionException("Startup failed", e);
        }
    }

    protected void runFramework() throws BundleException, InterruptedException {
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
    }

    protected void initFramework() throws Exception {
        getLog().info("Init the framework");
        FrameworkFactory factory = defaultRunner.getFrameworkFactory();
        getLog().debug("Using framework factory: " + factory);
        framework = factory.newFramework(mergeProperties());
        framework.init();
        initShutdownHook();
    }

    protected Set<File> getBundles() throws ArtifactNotFoundException, ArtifactResolutionException, MojoExecutionException, IOException, DependencyResolutionException, DependencyCollectionException {
        Set<File> bundles = new HashSet<>();
        String projectBundle = project.getBuild().getDirectory() + File.separator + project.getBuild().getFinalName() + BUNDLE_EXTESION;

        bundles.add(new File(projectBundle));

        for (Object obj : project.getArtifacts()) {
            Artifact dependency = (Artifact) obj;
            if (dependency.getFile().getName().endsWith(BUNDLE_EXTESION)) {
                bundles.add(dependency.getFile());
            }
        }

        addPlatformBundles(bundles);
        return bundles;
    }

    private void addPlatformBundles(Set<File> bundles) throws IOException, DependencyResolutionException, DependencyCollectionException {
        ArtifactResolver resolver = getArtifactResolver();
        resolver.addUnresolvedArtifact(getPlatformArtifact());
        resolver.resolveArtifacts();

        for (File f : resolver.getResolvedFiles()) {
            if (f.getName().endsWith(BUNDLE_EXTESION)) {
                bundles.add(f);
            }
        }
    }

    protected Map<String, String> mergeProperties() {
        if (configProps == null) {
            return DEFAULT_PROPS;
        }
        Map<String, String> props = new HashMap<>(DEFAULT_PROPS);
        for (Map.Entry<String, String> entry : configProps.entrySet()) {
            props.put(entry.getKey(),
                    entry.getValue());
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

    protected org.sonatype.aether.artifact.Artifact getPlatformArtifact() throws IOException {
        Properties props = new Properties();
        try (InputStream propStream = getClass().getResourceAsStream("/plugin.properties")) {
            props.load(propStream);
        }
        return new DefaultArtifact(props.getProperty("platform.coordinate"));
    }

    protected ArtifactResolver getArtifactResolver() {
        if (artifactResolver == null) {
            artifactResolver = new ArtifactResolver(repoSystem, repoSession, remoteRepos);
        }
        return artifactResolver;
    }
}
