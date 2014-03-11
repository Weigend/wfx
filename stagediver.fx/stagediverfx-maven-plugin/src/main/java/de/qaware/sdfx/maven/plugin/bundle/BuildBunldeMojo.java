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

import com.sun.javafx.tools.packager.DeployParams;
import com.sun.javafx.tools.packager.PackagerException;
import com.sun.javafx.tools.packager.PackagerLib;
import com.sun.javafx.tools.packager.bundlers.Bundler;
import org.apache.maven.model.License;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * Build a platform dependendend bundle.
 */
@Mojo(name = "build-bundle", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
public class BuildBunldeMojo extends AbstractMojo {

    /**
     * The output directory into which to copy the resources.
     */
    @Parameter(defaultValue = "${project.build.directory}/bundle", required = true)
    protected File outputDirectory;
    /**
     * The list of resources we want to transfer.
     */

    @Parameter(defaultValue = "${project.resources}", required = true, readonly = true)
    protected List<Resource> resources;
    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;
    protected PackagerLib packager = new PackagerLib();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        DeployParams deployParams = new DeployParams();
        deployParams.setApplicationClass("de.qaware.sdfx.main.Main");
        deployParams.setVersion(project.getVersion());
        deployParams.setAppName(project.getName());
        deployParams.setDescription(project.getDescription());
        StringBuilder builder = new StringBuilder();
        for (License lic : project.getLicenses()) {
            builder.append(lic.getName()).append(": ").append(lic.getUrl()).append("\n");
        }
        deployParams.setLicenseType(builder.toString());
        deployParams.setOutdir(outputDirectory);
        deployParams.setBundleType(Bundler.BundleType.ALL);
        deployParams.setVendor(project.getOrganization().getName() + "\n" + project.getOrganization().getUrl());
        try {
            packager.generateDeploymentPackages(deployParams);
        }
        catch (PackagerException e) {
            e.printStackTrace();
        }
    }
}
