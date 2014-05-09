// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 03.04.14 15:56
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.maven.plugin;

import de.qaware.sdfx.maven.plugin.resolver.ArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for Mojos that need the stagediver.fx platform bundles.
 *
 * @author christian.fritz
 */
public abstract class AbstractBundleResolverMojo extends AbstractMojo {

    private static final String BUNDLE_EXTESION = ".jar";

    /**
     * The artifact resolver.
     */
    protected ArtifactResolver artifactResolver;

    /**
     * The Maven project.
     */
    @Component
    protected MavenProject project;

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

    /**
     * Get a set of all bundles they should be loaded when starting the osgi framework.
     *
     * @return A set of all bundles the should be loaded.
     * @throws org.apache.maven.plugin.MojoExecutionException In case of the required stagediver.fx platform bundles can not be resolved.
     */
    protected Set<File> getBundles() throws MojoExecutionException {
        Set<File> bundles = new HashSet<>();
        String projectBundle = project.getBuild().getDirectory() +
                File.separator + project.getBuild().getFinalName() + BUNDLE_EXTESION;

        bundles.add(new File(projectBundle));

        for (Object obj : project.getArtifacts()) {
            Artifact dependency = (Artifact) obj;
            if (dependency.getFile().getName().endsWith(BUNDLE_EXTESION)) {
                getLog().debug("Adding dependency " + dependency.getFile() + " as bundle");
                bundles.add(dependency.getFile());
            }
        }
        addPlatformBundles(bundles);
        return bundles;
    }

    /**
     * Resolve the required stagediver.fx platform bundles and add it to the bundles param.
     *
     * @param bundles Add the additional platform bundles to this set.
     * @throws org.apache.maven.plugin.MojoExecutionException In case of the bundles can not be resolved.
     */
    private void addPlatformBundles(Set<File> bundles) throws MojoExecutionException {
        try {
            ArtifactResolver resolver = getArtifactResolver();
            addPlatformArtifact();
            resolver.resolveArtifacts();

            for (de.qaware.sdfx.maven.plugin.resolver.Artifact f : resolver.getResolvedArtifacts()) {
                if (f.getFile().getName().endsWith(BUNDLE_EXTESION)) {
                    getLog().debug("Adding dependency " + f + " as automatic platform bundle");
                    bundles.add(f.getFile());
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Can not read stagediver.fx platform bundle coordinates", e);
        }
    }

    /**
     * Get the artifact resolver.
     *
     * @return The artifact resolver.
     */
    protected ArtifactResolver getArtifactResolver() {
        if (artifactResolver == null) {
            artifactResolver = new ArtifactResolver(repoSystem, repoSession, remoteRepos);
        }
        return artifactResolver;
    }

    /**
     * Add the stagediver.fx platform meta dependency as additional dependency to the unresolved artifact list.
     *
     * @throws java.io.IOException In case of the coordinates for the dependency can not be read.
     */
    protected void addPlatformArtifact() throws IOException {
        de.qaware.sdfx.maven.plugin.resolver.Artifact platformArtifact =
                new de.qaware.sdfx.maven.plugin.resolver.Artifact(
                        "de.qaware.stagediver.fx", "platform-full",
                        ((PluginDescriptor) getPluginContext().get("pluginDescriptor")).getVersion(), "pom", null);

        getArtifactResolver().addUnresolvedArtifact(platformArtifact);
    }
}
