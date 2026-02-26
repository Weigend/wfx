/*
 * #%L
 * The platform-runner module is main start module for the wfx platform.
 * %%
 * Copyright (C) 2013 - 2015 Weigend AM
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
package io.softwareecg.wfx.main;

import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.lookup.LookupStrategy;
import io.softwareecg.wfx.lookup.impl.ServiceLoaderLookupStrategy;
import io.softwareecg.wfx.platform.api.EventBus;
import io.softwareecg.wfx.platform.api.Module;
import io.softwareecg.wfx.platform.api.PlatformApplication;
import io.softwareecg.wfx.windowmtg.api.GuiTestHelper;
import javafx.stage.Stage;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.any;

/**
 * Unit test for the {@link Main} class.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MainTest {

    @InjectMocks
    private Main main;

    @Mock
    private LookupStrategy lookupStrategy;
    @Mock
    private PlatformApplication platformApplication;
    @Mock
    private EventBus eventBus;

    private List<Module> modules;

    @Before
    public void setUp() throws Exception {
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(PlatformApplication.class)).thenReturn(platformApplication);
        when(lookupStrategy.lookup(EventBus.class)).thenReturn(eventBus);
        modules = new ArrayList<>();
        when(lookupStrategy.lookupAll(Module.class)).thenReturn(modules);
        mockModule("Test 1", "1.0");
        mockModule("Test 2", "1.1");
        FieldUtils.writeField(main, "modules", modules, true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInit() throws Exception {
        main.init();
        assertThat(Lookup.getLookupStrategy(), is(lookupStrategy));

        PlatformApplication platformApplication = (PlatformApplication) FieldUtils.readField(main, "platformApplication", true);
        List<Module> modules = (List<Module>) FieldUtils.readField(main, "modules", true);
        assertThat(platformApplication, is(notNullValue()));
        assertThat(modules.size(), is(equalTo(2)));
    }

    @Test
    public void testInitNoLookupStrategy() throws Exception {
        Lookup.init(null);
        main.init();
        assertThat(Lookup.getLookupStrategy(), is(instanceOf(ServiceLoaderLookupStrategy.class)));
    }

    @Test
    public void testStart() throws Exception {
        Stage stage = GuiTestHelper.getStage();
        GuiTestHelper.runInJavaFxThreadAndWait(() -> main.start(stage));
        Thread.sleep(1000);
        verify(platformApplication).preload();
        verify(platformApplication).showPreloader(any(Stage.class));
        for (Module module : modules) {
            verify(module).preload();
        }
        verify(platformApplication).hidePreloader();
        verify(platformApplication).showMainApplicationWindow(stage);
        verify(platformApplication).start();
        for (Module module : modules) {
            verify(module).start();
        }
    }

    @Test
    public void testStop() throws Exception {
        main.stop();
        verify(platformApplication).stop();
        for (Module module : modules) {
            verify(module).stop();
        }
    }

    private void mockModule(String name, String version) {
        Module module = mock(Module.class);
        when(module.getName()).thenReturn(name);
        when(module.getVersion()).thenReturn(version);
        modules.add(module);
    }
}
