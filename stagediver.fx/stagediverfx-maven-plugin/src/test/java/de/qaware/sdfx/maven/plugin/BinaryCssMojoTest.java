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
import com.sun.javafx.tools.packager.PackagerLib;
import com.sun.javafx.tools.resource.PackagerResource;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit test for the {@link BinaryCssMojo}.
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class BinaryCssMojoTest {

    private BinaryCssMojo mojo = new BinaryCssMojo();
    @Captor
    private ArgumentCaptor<CreateBSSParams> bssParamsCaptor;
    @Captor
    private ArgumentCaptor<Resource> resourceCaptor;

    @After
    public void tearDown() throws Exception {
        mojo.outputDirectory.delete();
    }

    private void initTest(String resourceDir, String outputDir) {
        Resource res = new Resource();
        res.setDirectory(getClass().getResource(resourceDir).getFile());
        mojo.resources = new ArrayList<>();
        mojo.resources.add(res);
        mojo.project = mock(MavenProject.class);
        mojo.packager = mock(PackagerLib.class);
        URL resource = getClass().getResource(outputDir);
        if (resource != null) {
            mojo.outputDirectory = new File(resource.getFile());
        }
        else {
            mojo.outputDirectory = new File(getClass().getResource("/").getFile() + "didNotExists");
        }
    }


    @Test
    @SuppressWarnings("unchecked")
    public void testExecute() throws Exception {
        initTest("/", "/");
        mojo.execute();

        // Verify the CreateBSSParams argument
        verify(mojo.packager).generateBSS(bssParamsCaptor.capture());
        assertEquals(1, bssParamsCaptor.getAllValues().size());

        CreateBSSParams params = bssParamsCaptor.getValue();
        List<PackagerResource> resources = (List<PackagerResource>) Whitebox.getInternalState(params, "resources");
        assertEquals(1, resources.size());
        assertEquals("bincss/test.css", resources.get(0).getRelativePath());

        // Verify the project resources
        verify(mojo.project).addResource(resourceCaptor.capture());
        assertEquals(1, resourceCaptor.getAllValues().size());
        assertEquals(mojo.outputDirectory.getAbsolutePath(), resourceCaptor.getValue().getDirectory());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteNotExists() throws Exception {
        initTest("/", "/DidNotExists");
        when(mojo.project.getResources()).thenReturn(Arrays.asList(getResource(mojo.outputDirectory)));
        mojo.execute();

        // Verify the project resources
        verify(mojo.project, never()).addResource(any(Resource.class));
    }

    private Resource getResource(File file) {
        Resource resource = new Resource();
        resource.setDirectory(file.getAbsolutePath());
        return resource;
    }
}
