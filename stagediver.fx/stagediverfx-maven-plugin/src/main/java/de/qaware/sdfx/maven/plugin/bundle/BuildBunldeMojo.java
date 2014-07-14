// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 11.02.14 09:12
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.maven.plugin.bundle;

import com.sun.javafx.tools.packager.PackagerLib;
import com.sun.javafx.tools.packager.bundlers.BundleParams;
import com.sun.javafx.tools.packager.bundlers.Bundler;
import com.sun.javafx.tools.packager.bundlers.RelativeFileSet;
import de.qaware.sdfx.maven.plugin.AbstractBundleResolverMojo;
import de.qaware.sdfx.maven.plugin.resolver.Artifact;
import org.apache.maven.model.License;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Build a platform dependendend bundle.
 *
 * @author christian.fritz
 */
//@Mojo(name = "build-bundle", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class BuildBunldeMojo extends AbstractBundleResolverMojo {

    /**
     * The path of all osgi bundles within the installer.
     */
    public static final String OSGI_BUNDLES_DIR_NAME = "bundles";

    /**
     * The output directory into which to copy the resources.
     */
    @Parameter(defaultValue = "${project.build.directory}/bundle", required = true)
    protected File outputDirectory;

    private File osOutputDirectory = new File(outputDirectory, "osBundles");

    /**
     * The list of resources we want to transfer.
     */
    @Parameter(defaultValue = "${project.resources}", required = true, readonly = true)
    protected List<Resource> resources;

    private Set<File> fileResources = new HashSet<>();

    protected PackagerLib packager = new PackagerLib();
    private File osgiBundlesDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        BundleParams bundleParams = new BundleParams();
        bundleParams.setApplicationClass("de.qaware.sdfx.main.Main");
        bundleParams.setAppVersion(project.getVersion());
        bundleParams.setName(project.getName());
        bundleParams.setDescription(project.getDescription());
        StringBuilder builder = new StringBuilder();
        for (License lic : project.getLicenses()) {
            builder.append(lic.getName()).append(": ").append(lic.getUrl()).append('\n');
        }
        bundleParams.setLicenseType(builder.toString());
        bundleParams.setVendor(project.getOrganization().getName() + "\n" + project.getOrganization().getUrl());
        bundleParams.setBundleFormat("any");
        bundleParams.setType(Bundler.BundleType.ALL);
        try {
            createDirectoryStructure();
            copyRunner();
            copyBundles();
            bundleParams.setAppResource(new RelativeFileSet(outputDirectory, fileResources));
            List<Bundler> bundlers = Bundler.get(bundleParams, true);
            for (Bundler bundler : bundlers) {
                bundler.bundle(bundleParams, osOutputDirectory);
            }

        } catch (IOException e) {
            throw new MojoFailureException("Can not build install bundle", e);
        }
    }

    private void copyRunner() throws MojoExecutionException, IOException {
        getArtifactResolver().addUnresolvedArtifact(new Artifact("de.qaware.stagediver.fx", "platform-runner", ((PluginDescriptor) getPluginContext().get("pluginDescriptor")).getVersion(), null));
        getArtifactResolver().resolveArtifacts();

        for (Artifact artifact : getArtifactResolver().getResolvedArtifacts()) {
            File srcFile = artifact.getFile();
            File destFile = new File(outputDirectory, srcFile.getName());
            Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            fileResources.add(destFile);
        }
    }

    private void copyBundles() throws MojoExecutionException, IOException {
        setArtifactResolver(null);
        Set<File> bundles = getBundles();

        for (File file : bundles) {
            File destFile = new File(osgiBundlesDir, file.getName());
            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            fileResources.add(destFile);
        }
    }

    private void createDirectoryStructure() throws MojoExecutionException {
        osgiBundlesDir = new File(outputDirectory, OSGI_BUNDLES_DIR_NAME);
        if (!osgiBundlesDir.exists() && !osgiBundlesDir.mkdirs()) {
            throw new MojoExecutionException("Can not create OSGi bundles directory.");
        }
    }
}
