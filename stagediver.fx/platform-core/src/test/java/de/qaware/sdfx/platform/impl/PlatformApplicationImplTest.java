package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.TestApplication;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.stage.Stage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PlatformApplicationImplTest {

    @Mock
    private WindowManager windowManager;
    @Mock
    private ApplicationWindow applicationWindow;
    @Mock
    private LookupStrategy lookupStrategy;
    @Mock
    private Stage stage;

    private PlatformApplication application;

    @BeforeClass
    public static void setUpClass() {
        TestApplication.launchTest();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        TestApplication.stopTest();
    }

    @Before
    public void setUp() throws Exception {
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(WindowManager.class)).thenReturn(windowManager);
        when(lookupStrategy.lookupAll(ApplicationWindow.class)).thenReturn(Arrays.asList(applicationWindow));
        when(lookupStrategy.lookup(EventBus.class)).thenReturn(mock(EventBus.class));
        application = new PlatformApplicationImpl();
    }

    @Test
    public void testGetName() throws Exception {
        String actual = application.getName();
        assertThat(actual, is(equalTo("Platform Core Application")));
    }

    @Test
    public void testGetVersion() throws Exception {
        String actual = application.getVersion();
        assertThat(actual, is(equalTo("")));
    }
}