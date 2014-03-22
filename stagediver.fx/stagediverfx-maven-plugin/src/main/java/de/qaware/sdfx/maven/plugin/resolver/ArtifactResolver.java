// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 13.12.13 16:54
//      description: Resolve transitive artifacts for given dependencies
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.maven.plugin.resolver;

import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Resolve the final artifacts including all transitive artifacts from a set of maven artifacts.
 *
 * @author christian.fritz
 */
public class ArtifactResolver {

    /**
     * The entry point to Aether, i.e. the component doing all the work.
     */
    private RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     */
    private RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution.
     */
    private List<RemoteRepository> remoteRepos;

    /**
     * A set with all artifacts they can be successfully resolved.
     */
    private Set<Artifact> artifacts = new HashSet<>();

    /**
     * A set with all artifacts the currently are unresolved.
     */
    private Set<org.sonatype.aether.artifact.Artifact> artifactsToResolve = new HashSet<>();

    /**
     * Initiate a new artifact resolver.
     *
     * @param repoSystem  The used repository system.
     * @param repoSession Use this session for resolving.
     * @param remoteRepos Search in this repositories for artifacts.
     */
    public ArtifactResolver(RepositorySystem repoSystem, RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos) {
        this.repoSystem = repoSystem;
        this.repoSession = repoSession;
        this.remoteRepos = remoteRepos;
    }

    /**
     * Add a artifact that should be resolved.
     *
     * @param artifact The unresolved artifact
     */
    public void addUnresolvedArtifact(Artifact artifact) {
        artifactsToResolve.add(artifact.asAetherArtifact());
    }

    /**
     * Add a artifact that should be resolved.
     *
     * @param artifact The unresolved artifact
     */
    public void addUnresolvedArtifact(org.sonatype.aether.artifact.Artifact artifact) {
        artifactsToResolve.add(artifact);
    }

    /**
     * Get a list with all files that are resolved.
     *
     * @return The resolved file set.
     */
    public Set<Artifact> getResolvedArtifacts() {
        return Collections.unmodifiableSet(artifacts);
    }

    /**
     * Resolve the added unresolved artifacts.
     *
     * @throws MojoExecutionException In case of the dependency resolution failed.
     */
    public void resolveArtifacts() throws MojoExecutionException {
        try {
            for (org.sonatype.aether.artifact.Artifact artifact : artifactsToResolve) {
                artifacts.addAll(resolveArtifact(artifact));
                artifactsToResolve.remove(artifact);
            }
        }
        catch (DependencyCollectionException | DependencyResolutionException e) {
            throw new MojoExecutionException("Dependency Resolution of platform bundle failed", e);
        }
    }

    /**
     * Resolve a artifact.
     *
     * @param artifact The unresolved artifact.
     * @return A list with all files of the resolved artifact.
     * @throws DependencyResolutionException In case of the dependencies can not be resolved.
     * @throws DependencyCollectionException In case of the dependencies can not be collected.
     */
    private List<Artifact> resolveArtifact(org.sonatype.aether.artifact.Artifact artifact) throws DependencyResolutionException, DependencyCollectionException {
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        request.setRepositories(remoteRepos);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, "runtime"));
        collectRequest.setRepositories(remoteRepos);
        DependencyNode node = repoSystem.collectDependencies(repoSession, collectRequest).getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest(node, null);

        repoSystem.resolveDependencies(repoSession, dependencyRequest);

        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
        node.accept(nlg);

        List<Artifact> artifactList = new ArrayList<>();
        for (org.sonatype.aether.artifact.Artifact artifact1 : nlg.getArtifacts(false)) {
            artifactList.add(Artifact.fromArtifact(artifact1));
        }

        return artifactList;
    }

    /**
     * Add the stagediver.fx platform meta dependency as additional dependency to the unresolved artifact list.
     *
     * @throws IOException In case of the coordinates for the dependency can not be read.
     */
    public void addPlatformArtifact() throws IOException {
        Properties props = new Properties();
        try (InputStream propStream = ArtifactResolver.class.getResourceAsStream("/plugin.properties")) {
            props.load(propStream);
        }
        addUnresolvedArtifact(new DefaultArtifact(props.getProperty("platform.coordinate")));
    }
}
