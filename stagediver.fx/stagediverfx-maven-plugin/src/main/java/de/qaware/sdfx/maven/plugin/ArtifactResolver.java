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

package de.qaware.sdfx.maven.plugin;

import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolve the final artifacts including all transitive artifacts from a set of maven artifacts.
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

    private Set<File> artifacts = new HashSet<>();

    private Set<Artifact> artifactsToResolve = new HashSet<>();

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
        artifactsToResolve.add(artifact);
    }

    /**
     * Get a list with all files that are resolved.
     *
     * @return The resolved file set.
     */
    public Set<File> getResolvedFiles() {
        return Collections.unmodifiableSet(artifacts);
    }

    /**
     * Resolve the added unresolved artifacts.
     *
     * @throws DependencyResolutionException
     * @throws DependencyCollectionException
     */
    public void resolveArtifacts() throws DependencyResolutionException, DependencyCollectionException {
        for (Artifact artifact : artifactsToResolve) {
            artifacts.addAll(resolveArtifact(artifact));
            artifactsToResolve.remove(artifact);
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
    private List<File> resolveArtifact(Artifact artifact) throws DependencyResolutionException, DependencyCollectionException {
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
        return nlg.getFiles();
    }
}
