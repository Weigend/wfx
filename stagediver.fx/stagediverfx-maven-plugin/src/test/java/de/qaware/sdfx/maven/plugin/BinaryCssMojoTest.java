package de.qaware.sdfx.maven.plugin;

import com.sun.javafx.tools.packager.CreateBSSParams;
import com.sun.javafx.tools.packager.PackagerLib;
import com.sun.javafx.tools.resource.PackagerResource;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BinaryCssMojoTest {

    private BinaryCssMojo mojo = new BinaryCssMojo();

    @Before
    public void setUp() throws Exception {
        Resource res = new Resource();

        res.setDirectory(getClass().getResource("/").getFile());
        mojo.resources = new ArrayList<>();
        mojo.resources.add(res);
        mojo.project = mock(MavenProject.class);
        mojo.packager = mock(PackagerLib.class);
        mojo.outputDirectory = new File(getClass().getResource("/").getFile());
    }

    @Test
    public void testExecute() throws Exception {
        mojo.execute();

        ArgumentCaptor<CreateBSSParams> bssParamsCaptor = ArgumentCaptor.forClass(CreateBSSParams.class);

        // Verify the CreateBSSParams argument
        verify(mojo.packager).generateBSS(bssParamsCaptor.capture());
        assertEquals(1, bssParamsCaptor.getAllValues().size());
        CreateBSSParams params = bssParamsCaptor.getValue();
        Field resourcesField = CreateBSSParams.class.getDeclaredField("resources");
        resourcesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        ArrayList<PackagerResource> resources = (ArrayList<PackagerResource>) resourcesField.get(params);
        assertEquals(1, resources.size());
        assertEquals("bincss/test.css", resources.get(0).getRelativePath());

        // Verify the project resources
        ArgumentCaptor<Resource> resourceCaptor = ArgumentCaptor.forClass(Resource.class);
        verify(mojo.project).addResource(resourceCaptor.capture());
        assertEquals(1, resourceCaptor.getAllValues().size());
        assertEquals(mojo.outputDirectory.getAbsolutePath(), resourceCaptor.getValue().getDirectory());
    }
}
