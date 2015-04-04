/*
 * #%L
 * The stagediver.fx maven plugin brings helper to build, test and run your stagediver.fx application.
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.qaware.sdfx.maven.plugin;

import com.sun.javafx.tools.packager.CreateBSSParams;
import com.sun.javafx.tools.packager.PackagerException;
import com.sun.javafx.tools.packager.PackagerLib;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.util.List;

/**
 * This maven plugin compiles all css files into bss to use them with javafx.
 * <p/>
 * It binds to the process-resources phase and compiles all cascading stylesheets (CSS) to binary style sheets (BSS).
 * See {@see http://docs.oracle.com/javafx/2/deployment/javafx_ant_task_reference.htm#CIAEFCGA} for more information
 * about the binary stylesheets.
 *
 * @author christian.fritz
 */
@Mojo(
        name = "binary-css",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        requiresProject = true
)
public class BinaryCssMojo extends AbstractMojo {

    /**
     * The output directory into which to copy the resources.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
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
        removeOldBssFiles();
        getLog().info("Compiling CSS to BSS");

        CreateBSSParams bssParams = new CreateBSSParams();
        for (Resource resource : resources) {
            addCssForResource(bssParams, resource);
        }
        bssParams.setOutdir(outputDirectory);
        bssParams.setVerbose(getLog().isDebugEnabled());
        try {
            if (outputDirectory.exists() || outputDirectory.mkdirs()) {
                packager.generateBSS(bssParams);
                updateProjectResources();
                getLog().info("Compiling CSS successfully");
            }
            else {
                throw new MojoExecutionException("Can not create output directory '" + outputDirectory + "'.");
            }
        }
        catch (PackagerException e) {
            throw new MojoExecutionException("Can not generate binary style sheets", e);
        }
    }

    /**
     * Add a resource to the bss params for compiling the css file.
     *
     * @param bssParams The bss params.
     * @param resource  The resources they should be included.
     */
    private void addCssForResource(CreateBSSParams bssParams, Resource resource) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{"**/*.css"});
        scanner.setBasedir(resource.getDirectory());
        scanner.scan();
        for (String file : scanner.getIncludedFiles()) {
            bssParams.addResource(new File(resource.getDirectory()), file);
            getLog().debug("Compile CSS: " + file);
        }
    }

    /**
     * Remove all old binary style sheets if there are any.
     */
    private void removeOldBssFiles() {
        if (!outputDirectory.exists()) {
            return;
        }
        getLog().debug("Remove old bss files for recompiling");
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{"**/*.bss"});
        scanner.setBasedir(outputDirectory);
        scanner.scan();
        for (String file : scanner.getIncludedFiles()) {
            String absolutePath = outputDirectory.getAbsoluteFile() + File.separator + file;
            if (!new File(absolutePath).delete()) {
                getLog().error("Can not delete binary style sheet file: " + absolutePath);
            }
        }
    }

    /**
     * Update the Maven project resources.
     */
    private void updateProjectResources() {
        // now add the descriptor directory to the maven resources
        final String outputResourceDir = this.outputDirectory.getAbsolutePath();
        boolean found = false;

        for (Object resource : project.getResources()) {
            found = ((Resource) resource).getDirectory().equals(outputResourceDir);
        }
        if (!found) {
            final Resource resource = new Resource();
            resource.setDirectory(this.outputDirectory.getAbsolutePath());
            this.project.addResource(resource);
        }
    }
}
