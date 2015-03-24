package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.JavaFXThreadingRule;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javafx.stage.*;
import java.util.Arrays;

import static de.qaware.sdfx.windowmtg.api.GuiTestHelper.getStage;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PlatformApplicationImplTest {

    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();

    @Mock
    private WindowManager windowManager;
    @Mock
    private ApplicationWindow applicationWindow;
    @Mock
    private LookupStrategy lookupStrategy;

    private PlatformApplication application;

    @Before
    public void setUp() throws Exception {
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(WindowManager.class)).thenReturn(windowManager);
        when(lookupStrategy.lookupAll(ApplicationWindow.class)).thenReturn(Arrays.asList(applicationWindow));
        when(applicationWindow.getStage()).thenAnswer(invocationOnMock -> getStage());
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

    @Test
    public void testShowHidePreloader() throws Exception {
        Stage stage = new Stage();
        stage.initOwner(getStage());
        application.showPreloader(stage);
        assertThat(stage.isShowing(), is(equalTo(true)));
        application.hidePreloader();
        assertThat(stage.isShowing(), is(equalTo(false)));
    }

    @Test
    public void testShowMainApplicationWindowAndStop() throws Exception {
        Stage stage = new Stage();
        stage.initOwner(getStage());
        application.showMainApplicationWindow(stage);
        assertThat(stage.isShowing(), is(equalTo(true)));
        application.stop();
        assertThat(stage.isShowing(), is(equalTo(false)));
        verify(applicationWindow).setStage(stage);
        verify(applicationWindow).setWindowManager(windowManager);
        verify(applicationWindow).init();
        verify(windowManager).init();
    }
}
