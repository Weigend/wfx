package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javafx.application.*;
import javafx.stage.*;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

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

    @Before
    public void setUp() throws Exception {
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(WindowManager.class)).thenReturn(windowManager);
        when(lookupStrategy.lookupAll(ApplicationWindow.class)).thenReturn(Arrays.asList(applicationWindow));
        when(applicationWindow.getStage()).thenReturn(stage);
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
    @Ignore("TODO Refactor it due to test fail but it is not reported")
    public void testShowHidePreloader() throws Exception {
        Platform.runLater(() -> {
            try {
                application.showPreloader(stage);
                assertThat(stage.isShowing(), is(equalTo(true)));
                application.hidePreloader();
                assertThat(stage.isShowing(), is(equalTo(false)));
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    @Ignore("TODO Refactor it due to test fail but it is not reported")
    public void testShowMainApplicationWindowAndStop() throws Exception {
        Platform.runLater(() -> {
            try {
                application.showMainApplicationWindow(stage);
                assertThat(stage.isShowing(), is(equalTo(true)));
                application.stop();
                assertThat(stage.isShowing(), is(equalTo(false)));
                verify(applicationWindow).setStage(stage);
                verify(applicationWindow).setWindowManager(windowManager);
                verify(applicationWindow).init();
                verify(applicationWindow).getStage().show();
                verify(windowManager).init();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
