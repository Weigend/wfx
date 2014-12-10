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

package de.qaware.sdfx.maven.plugin.run;

import de.qaware.sdfx.main.Main;
import de.qaware.sdfx.maven.plugin.resolver.Artifact;
import de.qaware.sdfx.maven.plugin.resolver.ArtifactResolver;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

/**
 * Test for the stagediver.fx run mojo
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class RunMojoTest {
    @InjectMocks
    private RunMojo mojo;

    @Mock
    private ArtifactResolver resolver;


    @Mock
    private MavenProject project;

    @Mock
    private Build projectBuild;

    @Mock
    private Main runner;

    @Before
    public void setUp() throws Exception {
        when(project.getBuild()).thenReturn(projectBuild);
        when(projectBuild.getOutputDirectory()).thenReturn("outputDir");
        when(projectBuild.getFinalName()).thenReturn("finalName");

        Set<Artifact> resolvedFiles = new HashSet<>();
        resolvedFiles.add(new Artifact("group", "artifact", "1.0", new File("test.jar")));
        resolvedFiles.add(new Artifact("group", "artifact-war", "1.0", new File("test.war")));
        initPluginContext();

        when(resolver.getResolvedArtifacts()).thenReturn(resolvedFiles);
    }

    private void initPluginContext() {
        PluginDescriptor pluginDescriptor = new PluginDescriptor();
        HashMap<String, Object> context = new HashMap<>();
        context.put("pluginDescriptor", pluginDescriptor);
        pluginDescriptor.setVersion("1.0.0");
        mojo.setPluginContext(context);
    }


    @Test
    public void testExecute() throws Exception {
        mojo.execute();

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

    }

    @Test
    public void testRunFramework() throws Exception {
        mojo.runFramework();

    }
}
