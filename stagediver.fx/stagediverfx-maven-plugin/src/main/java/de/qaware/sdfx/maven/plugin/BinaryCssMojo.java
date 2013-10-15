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


@Mojo(name = "binary-css", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, requiresProject = true)
public class BinaryCssMojo extends AbstractMojo {

    /**
     * The output directory into which to copy the resources.
     */
    @Parameter(defaultValue = "${project.build.directory}/binary-css", required = true)
    private File outputDirectory;
    /**
     * The list of resources we want to transfer.
     */

    @Parameter(defaultValue = "${project.resources}", required = true, readonly = true)
    private List<Resource> resources;
    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    private PackagerLib packager = new PackagerLib();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        CreateBSSParams bssParams = new CreateBSSParams();

        for (Resource resource : resources) {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setIncludes(new String[]{"**/*.css"});
            scanner.setBasedir(resource.getDirectory());
            scanner.scan();
            for (String file : scanner.getIncludedFiles()) {
                bssParams.addResource(new File(resource.getDirectory()), file);
            }
        }
        bssParams.setOutdir(outputDirectory);
        bssParams.setVerbose(getLog().isDebugEnabled());
        try {
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            packager.generateBSS(bssParams);
            updateProjectResources();
        } catch (PackagerException e) {
            throw new MojoExecutionException("Can not generate binary style sheets", e);
        }
    }

    /**
     * Update the Maven project resources.
     */
    private void updateProjectResources() {
        // now add the descriptor directory to the maven resources
        final String ourRsrcPath = this.outputDirectory.getAbsolutePath();
        boolean found = false;

        for (Object resource : project.getResources()) {
            found = ((Resource) resource).getDirectory().equals(ourRsrcPath);
        }
        if (!found) {
            final Resource resource = new Resource();
            resource.setDirectory(this.outputDirectory.getAbsolutePath());
            this.project.addResource(resource);
        }
    }
}
