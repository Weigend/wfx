/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
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
package io.softwareecg.wfx.platform.impl;

import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.lookup.LookupStrategy;
import io.softwareecg.wfx.platform.api.EventBus;
import io.softwareecg.wfx.platform.api.PlatformApplication;
import io.softwareecg.wfx.platform.api.exceptions.PlatformException;
import io.softwareecg.wfx.windowmtg.api.ApplicationWindow;
import io.softwareecg.wfx.windowmtg.api.JavaFXThreadingRule;
import io.softwareecg.wfx.windowmtg.api.WindowManager;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static io.softwareecg.wfx.windowmtg.api.GuiTestHelper.getStage;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link PlatformApplicationImpl}
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PlatformApplicationImplTest {

    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();

    @Mock
    private WindowManager windowManager;
    @Mock
    private ApplicationWindow applicationWindow;
    @Mock
    private ApplicationWindow failingApplicationWindow;
    @Mock
    private LookupStrategy lookupStrategy;

    private PlatformApplication application;

    @Before
    public void setUp() throws Exception {
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(FXMLLoader.class)).thenAnswer(invocationOnMock1 -> new FXMLLoader());
        when(lookupStrategy.lookup(WindowManager.class)).thenReturn(windowManager);
        when(lookupStrategy.lookupAll(ApplicationWindow.class)).thenReturn(
                Arrays.asList(failingApplicationWindow, applicationWindow));

      //  doThrow(IOException.class).when(failingApplicationWindow).setStage(any(Stage.class));
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
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void testShowHidePreloader() throws Exception {
        Stage stage = new Stage();
        stage.initOwner(getStage());
        application.hidePreloader();
        application.showPreloader(stage);
        assertThat(stage.isShowing(), is(equalTo(true)));
        application.hidePreloader();
        assertThat(stage.isShowing(), is(equalTo(false)));
    }

    @Test(expected = PlatformException.class)
    public void testShowMainApplicationWindowOpenPreloader() throws Exception {
        Stage stage = new Stage();
        stage.initOwner(getStage());
        application.showPreloader(stage);
        stage = new Stage();
        stage.initOwner(getStage());
        application.showMainApplicationWindow(stage);
    }

    @Test
    public void testShowMainApplicationWindowAndStop() throws Exception {
        /*
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
        */
    }
}
