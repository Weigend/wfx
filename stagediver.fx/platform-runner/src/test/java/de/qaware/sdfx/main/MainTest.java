//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: platform-runner
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 24.03.15 19:03
//      description:
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.main;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy;
import de.qaware.sdfx.platform.api.Module;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.windowmtg.api.GuiTestHelper;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for the {@link Main} class.
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class MainTest {

    @InjectMocks
    private Main main;

    @Mock
    private LookupStrategy lookupStrategy;
    @Mock
    private PlatformApplication platformApplication;

    private List<Module> modules;

    @Before
    public void setUp() throws Exception {
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(PlatformApplication.class)).thenReturn(platformApplication);
        modules = new ArrayList<>();
        when(lookupStrategy.lookupAll(Module.class)).thenReturn(modules);
        mockModule("Test 1", "1.0");
        mockModule("Test 2", "1.1");
        Whitebox.setInternalState(main, "modules", modules);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInit() throws Exception {
        main.init();
        assertThat(Lookup.getLookupStrategy(), is(lookupStrategy));

        PlatformApplication platformApplication = (PlatformApplication) Whitebox.getInternalState(main, "platformApplication");
        List<Module> modules = (List<Module>) Whitebox.getInternalState(main, "modules");
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
