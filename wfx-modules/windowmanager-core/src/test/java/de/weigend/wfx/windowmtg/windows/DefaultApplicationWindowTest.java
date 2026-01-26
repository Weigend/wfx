/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 Weigend AM
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
package de.weigend.wfx.windowmtg.windows;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.lookup.LookupStrategy;
import de.weigend.wfx.platform.api.EventBus;
import de.weigend.wfx.windowmtg.api.WindowManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static de.weigend.wfx.windowmtg.api.GuiTestHelper.getStage;
import static de.weigend.wfx.windowmtg.api.GuiTestHelper.runInJavaFxThreadAndWait;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for the {@link DefaultApplicationWindow}.
 *
 * @author christian.fritz
 */
//@ R unWith(MockitoJUnitRunner.class)
public class DefaultApplicationWindowTest {

    /*
    private static LookupStrategy strategy = mock(LookupStrategy.class);
    @Mock
    private WindowEvent event;
    @Mock
    private WindowManager windowManager;
    @Mock
    private EventBus eventBus;

    @InjectMocks
    private DefaultApplicationWindow window = new DefaultApplicationWindow();

    static {
        Lookup.init(strategy);
        when(strategy.lookup(FXMLLoader.class)).thenAnswer(i -> new FXMLLoader());

    }

    @Before
    public void setUp() throws Exception {
        when(strategy.lookup(FXMLLoader.class)).thenAnswer(i -> new FXMLLoader());
        when(strategy.lookup(EventBus.class)).thenReturn(eventBus);
    }

    @Test
    public void testInit() throws Exception {
        runInJavaFxThreadAndWait(() -> FieldUtils.writeDeclaredField(window, "stage", new Stage()));
        runInJavaFxThreadAndWait(window::init);
        MenuBar menuBar = (MenuBar) FieldUtils.readDeclaredField(window, "menuBar");
        assertThat(menuBar.isUseSystemMenuBar(), is(equalTo(true)));
    }

    @Test
    public void testInitNoMenuBar() throws Exception {
        runInJavaFxThreadAndWait(() -> FieldUtils.writeDeclaredField(window, "stage", new Stage()));
        FXMLLoader loader = mock(FXMLLoader.class);
        when(strategy.lookup(FXMLLoader.class)).thenReturn(loader);
        when(loader.load()).thenReturn(new BorderPane());
        runInJavaFxThreadAndWait(window::init);
        assertThat(FieldUtils.readDeclaredField(window, "menuBar"), nullValue());
    }


    @Test
    public void testPlatformShutdownRequestHandler() throws Exception {
        window.setStage(getStage());
        Platform.runLater(() -> window.platformShutdownRequestHandler(event));
        Thread.sleep(500);
        DialogPane dialogPane = null;
        Button button = (Button) dialogPane.lookupButton(ButtonType.NO);
        runInJavaFxThreadAndWait(button::fire);
        Thread.sleep(200);
        verify(event).consume();
    }


    protected Parent getRootNode() {
        return new Label("");
    }

    */
}