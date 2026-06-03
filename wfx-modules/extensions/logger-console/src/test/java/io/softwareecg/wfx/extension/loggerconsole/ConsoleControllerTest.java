/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
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
package io.softwareecg.wfx.extension.loggerconsole;

import io.softwareecg.wfx.extension.loggerconsole.api.LoggerAdapter;
import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.lookup.api.LookupStrategy;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static io.softwareecg.wfx.windowmanager.testutil.GuiTestHelper.getStage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link ConsoleController}.
 *
 */
@RunWith(MockitoJUnitRunner.class)

public class ConsoleControllerTest {
    @Mock
    private LoggerAdapter adapter;
    @Mock
    private LookupStrategy strategy;
    @Spy
    private TextArea console;
    @Spy
    private TableView<LoggerAdapter.LoggerLevel> levels;
    @Spy
    private Button clear;

    @InjectMocks
    private ConsoleController controller;

    static {
        getStage();
    }

    @Before
    public void setUp() throws Exception {
        Lookup.init(strategy);
        when(strategy.lookup(LoggerAdapter.class)).thenReturn(adapter);
        when(adapter.messagesProperty()).thenReturn(new SimpleStringProperty());
        when(adapter.levelsProperty()).thenReturn(new SimpleListProperty<>(FXCollections.observableArrayList()));
        when(adapter.loggerLevelsProperty()).thenReturn(new SimpleListProperty<>(FXCollections.observableArrayList()));
        adapter.levelsProperty().addAll("ALL", "Trace", "Debug", "Info", "Warn", "Error");
        ((StringProperty) adapter.messagesProperty()).set("ABC");
        levels.getColumns().addAll(new TableColumn<>(), new TableColumn<>());
    }

    @Test
    public void testInitialize() throws Exception {
        controller.initialize(null, null);
        assertThat(console.getText(), is(equalTo("ABC")));
        assertThat(levels.getColumns().get(1).getCellFactory(), notNullValue());
    }
}