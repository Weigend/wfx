// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 03.04.14 16:32
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.maven.plugin;

import de.qaware.sdfx.maven.plugin.resolver.ArtifactResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link AbstractBundleResolverMojo}
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractBundleResolverMojoTest {

    @InjectMocks
    private BundleResolverTestMojo mojo;

    @Mock
    private ArtifactResolver resolver;

    @Mock
    private MavenProject project;

    @Mock
    private Build projectBuild;

    @Before
    public void setUp() throws Exception {
        when(project.getBuild()).thenReturn(projectBuild);

        when(projectBuild.getOutputDirectory()).thenReturn("outputDir");
        when(projectBuild.getFinalName()).thenReturn("finalName");
        Set<de.qaware.sdfx.maven.plugin.resolver.Artifact> resolvedFiles = new HashSet<>();
        resolvedFiles.add(new de.qaware.sdfx.maven.plugin.resolver.Artifact("group", "artifact", "1.0", new File("test.jar")));
        resolvedFiles.add(new de.qaware.sdfx.maven.plugin.resolver.Artifact("group", "artifact-war", "1.0", new File("test.war")));

        when(resolver.getResolvedArtifacts()).thenReturn(resolvedFiles);
    }

    @Test
    public void testGetBundles() throws Exception {
        Set<Artifact> artifacts = new HashSet<>();
        org.apache.maven.artifact.Artifact a1 = mock(org.apache.maven.artifact.Artifact.class);
        when(a1.getFile()).thenReturn(new File("test.jar"));
        org.apache.maven.artifact.Artifact a2 = mock(org.apache.maven.artifact.Artifact.class);
        when(a2.getFile()).thenReturn(new File("test.war"));
        artifacts.add(a1);
        artifacts.add(a2);
        when(project.getArtifacts()).thenReturn(artifacts);

        Set<File> actual = mojo.getBundles();
        assertThat(actual.size(), is(2));
    }
}
