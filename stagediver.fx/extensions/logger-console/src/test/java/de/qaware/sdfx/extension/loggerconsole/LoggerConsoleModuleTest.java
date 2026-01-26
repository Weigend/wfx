/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 QAware GmbH
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
package de.qaware.sdfx.extension.loggerconsole;

import de.qaware.sdfx.extension.uiutils.MenuUtil;
import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link LoggerConsoleModule}.
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggerConsoleModuleTest {
    @Mock
    private FXMLView<ConsoleController> view;
    @Mock
    private ApplicationWindow window;
    @Mock
    private WindowManager manager;
    @Mock
    private FXMLLoader loader;
    @Mock
    private LookupStrategy strategy;

    @InjectMocks
    private LoggerConsoleModule module;

    @Before
    public void setUp() throws Exception {
        Lookup.init(strategy);
        when(strategy.lookup(ApplicationWindow.class)).thenReturn(window);
        when(strategy.lookup(WindowManager.class)).thenReturn(manager);
        when(strategy.lookup(FXMLLoader.class)).thenReturn(loader);
        when(window.getMenu()).thenReturn(FXCollections.observableArrayList());
    }

    @Test
    public void testStart() throws Exception {
        when(manager.hasRegisteredView(view)).thenReturn(false, true);
        module.start();
        MenuItem showConsole = MenuUtil.findItem(window.getMenu(), "loggerConsole", true);
        showConsole.fire();
        verify(manager).register(view);
        showConsole.fire();
        verify(manager).showView(view);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPreload() throws Exception {
        module.preload();
        FXMLView<ConsoleController> consoleView = (FXMLView<ConsoleController>) Whitebox.getInternalState(module, "consoleView");
        assertThat(consoleView, notNullValue());
        assertThat(consoleView, is(not(sameInstance(view))));
    }
}