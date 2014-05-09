//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: stagediverfx-maven-plugin
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 30.03.14 19:55
//      description:
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.maven.plugin.resolver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class ArtifactResolverTest {
    @Mock
    private RepositorySystem repoSystem;

    @Mock
    private RepositorySystemSession repoSession;

    @Mock
    private List<RemoteRepository> remoteRepos;

    private ArtifactResolver resolver = new ArtifactResolver(repoSystem, repoSession, remoteRepos);

    @Test
    public void testAddUnresolvedArtifact() throws Exception {
        resolver.addUnresolvedArtifact(new Artifact("group", "artifact", "1.0", null));
        resolver.addUnresolvedArtifact(new Artifact("group", "artifact1", "1.1", null));
        Set<org.sonatype.aether.artifact.Artifact> unresolvedArtifacts = getUnresolvedArtifacts();
        assertThat(unresolvedArtifacts, hasSize(2));
    }

    private Set<org.sonatype.aether.artifact.Artifact> getUnresolvedArtifacts() throws NoSuchFieldException, IllegalAccessException {
        Field artifactsToResolve = ArtifactResolver.class.getDeclaredField("artifactsToResolve");
        artifactsToResolve.setAccessible(true);
        return (Set<org.sonatype.aether.artifact.Artifact>) artifactsToResolve.get(resolver);

    }
}
