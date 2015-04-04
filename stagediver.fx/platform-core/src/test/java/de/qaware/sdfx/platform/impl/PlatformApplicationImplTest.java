/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
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
package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.platform.api.exceptions.PlatformException;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.JavaFXThreadingRule;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javafx.fxml.*;
import javafx.stage.*;
import java.io.IOException;
import java.util.Arrays;

import static de.qaware.sdfx.windowmtg.api.GuiTestHelper.getStage;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit test for the {@link PlatformApplicationImpl}
 *
 * @author christian.fritz
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

        doThrow(IOException.class).when(failingApplicationWindow).setStage(any(Stage.class));
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
