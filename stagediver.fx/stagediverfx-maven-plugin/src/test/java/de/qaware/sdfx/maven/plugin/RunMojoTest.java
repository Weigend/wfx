// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 13.12.13 14:32
//      description: Run Mojo Test
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.maven.plugin;

import de.qaware.sdfx.main.Main;
import org.apache.felix.framework.FrameworkFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RunMojoTest {

    private RunMojo mojo;

    @Mock
    private FrameworkFactory factory;

    @Mock
    private Framework framework;
    @Mock
    private ArtifactResolver resolver;

    @Mock
    private BundleContext frameworkContext;

    @Mock
    private MavenProject project;

    @Mock
    private Build projectBuild;

    @Before
    public void setUp() throws Exception {
        mojo = new RunMojo();
        mojo.defaultRunner = mock(Main.class);
        mojo.framework = framework;
        mojo.artifactResolver = resolver;
        mojo.project = project;

        when(project.getBuild()).thenReturn(projectBuild);
        when(projectBuild.getOutputDirectory()).thenReturn("outputDir");
        when(projectBuild.getFinalName()).thenReturn("finalName");
        when(mojo.defaultRunner.getFrameworkFactory()).thenReturn(factory);
        when(factory.newFramework(anyMap())).thenReturn(framework);
        when(framework.getBundleContext()).thenReturn(frameworkContext);
        when(frameworkContext.getBundle(0)).thenReturn(framework);
        when(frameworkContext.getBundles()).thenReturn(new Bundle[]{framework});
        when(framework.adapt(FrameworkStartLevel.class)).thenReturn(mock(FrameworkStartLevel.class));
        when(framework.waitForStop(0)).thenReturn(
                new FrameworkEvent(FrameworkEvent.STOPPED_UPDATE, mock(Bundle.class), null),
                new FrameworkEvent(FrameworkEvent.STOPPED, mock(Bundle.class), null));
        Set<File> resolvedFiles = new HashSet<>();
        resolvedFiles.add(new File("test.jar"));
        resolvedFiles.add(new File("test.war"));

        when(resolver.getResolvedFiles()).thenReturn(resolvedFiles);
    }


    @Test
    //@Ignore
    public void testExecute() throws Exception {
        mojo.execute();

        verify(framework).init();

        verify(framework, times(2)).start();
        verify(framework, times(2)).waitForStop(0);
    }

    @Test
    public void testMergeProperties() throws Exception {
        mojo.configProps = new HashMap<>();
        assertThat(mojo.mergeProperties().size(), is(9));
        mojo.configProps.put("some.property", "value");
        assertThat(mojo.mergeProperties().size(), is(10));
        mojo.configProps.put("felix.log.level", "2");
        assertThat(mojo.mergeProperties().size(), is(10));
    }

    @Test
    public void testInitFramework() throws Exception {
        mojo.initFramework();
        verify(framework, times(1)).init();
    }

    @Test
    public void testRunFramework() throws Exception {


        mojo.runFramework();
        verify(framework, times(2)).start();
        verify(framework, times(2)).waitForStop(0);
    }

    @Test
    public void testGetBundles() throws Exception {

        Set<Artifact> artifacts = new HashSet<>();
        Artifact a1 = mock(Artifact.class);
        when(a1.getFile()).thenReturn(new File("test.jar"));
        Artifact a2 = mock(Artifact.class);
        when(a2.getFile()).thenReturn(new File("test.war"));
        artifacts.add(a1);
        artifacts.add(a2);
        when(project.getArtifacts()).thenReturn(artifacts);

        Set<File> actual = mojo.getBundles();
        assertThat(actual.size(), is(2));
    }

}
