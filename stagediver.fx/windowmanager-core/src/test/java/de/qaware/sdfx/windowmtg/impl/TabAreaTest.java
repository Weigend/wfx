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
package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.windowmtg.api.JavaFXThreadingRule;
import de.qaware.sdfx.windowmtg.api.Position;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javafx.collections.*;
import javafx.scene.control.*;
import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test for {@link TabArea}.
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class TabAreaTest {
    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();
    private TabArea tabArea;

    @Mock
    private DragNDropManager dragNDropManager;

    @Mock
    private MultiWindowManager windowManager;

    private RootArea rootArea;

    @Mock
    private ObservableList<Tab> tabs;

    @Mock
    private ViewArea parent;

    @Mock
    private LookupStrategy lookupStrategy;


    @Before
    public void setUp() throws Exception {
        when(lookupStrategy.lookup(ViewContainerAreaFactory.class)).thenReturn(new ViewContainerAreaFactoryMockImpl());
        Lookup.init(lookupStrategy);

        rootArea = new RootArea(dragNDropManager, true);
        when(dragNDropManager.getWindowManager()).thenReturn(windowManager);
        when(parent.getParent()).thenReturn(rootArea);

        //todo: replace tabs list
        tabArea = new TabArea(parent, dragNDropManager);
        TabPane tabPane = mock(TabPane.class);
        when(tabPane.getTabs()).thenReturn(FXCollections.observableArrayList());

        //tabs = new SimpleListProperty<>();
        Field tabsField = TabPane.class.getDeclaredField("tabs");
        tabsField.setAccessible(true);
        tabsField.set(tabPane, tabs);

        Field tabPaneField = TabArea.class.getDeclaredField("tabPane");
        tabPaneField.setAccessible(true);
        tabPaneField.set(tabArea, tabPane);
    }

    @Test
    public void testRemoveViewNotAssigned() throws Exception {
        ViewStatus view = mock(ViewStatus.class);
        tabArea.remove(view);
        verify(view, never()).setArea(null);
    }

  /*
    public void testAddRemove() throws Exception {
        ViewStatus status = mock(ViewStatus.class);
        when(status.getView()).thenReturn(new TestView("asdf", Position.TOP));
        when(status.getArea()).thenReturn(tabArea);
        Tab t = mock(Tab.class);
        when(status.getTab()).thenReturn(t);
        tabArea.add(status, Position.CENTER);
        verify(tabs).add(t);
        verify(status).setPosition(Position.CENTER);
        verify(status).setArea(tabArea);

        tabArea.remove(status, false);
        verify(status).setPosition(null);
        verify(status).setArea(null);
        verify(tabs).remove(t);
    }*/

   /*
    public void testAddRemoveHandleEmpty() throws Exception {
        ViewStatus status = mock(ViewStatus.class);
        when(status.getView()).thenReturn(new TestView("asdf", Position.TOP));
        when(status.getArea()).thenReturn(tabArea);
        Tab t = mock(Tab.class);
        when(status.getTab()).thenReturn(t);
        tabArea.add(status, Position.CENTER);

        tabArea.remove(status, true);
        verify(tabs).remove(t);
        verify(parent).remove(tabArea);
    }*/

    @Test
    public void testHandleEmptyWrong() throws Exception {
        ViewStatus status = mock(ViewStatus.class);
        tabArea.add(status, Position.CENTER);
        assertThat(tabArea.handleEmpty(), is(false));
    }

    @Test
    public void testAddNonCenter() throws Exception {
        ViewStatus status = mock(ViewStatus.class);
        when(status.getView()).thenReturn(new TestView("asdf", Position.TOP));
        when(status.getArea()).thenReturn(tabArea);
        ArgumentCaptor<ViewArea> viewAreaCaptor = ArgumentCaptor.forClass(ViewArea.class);

        tabArea.add(status, Position.TOP);

        verify(parent).replace(eq(tabArea), viewAreaCaptor.capture());
        assertThat(viewAreaCaptor.getValue().getSecondChild(), is((ViewArea) tabArea));
        assertThat(viewAreaCaptor.getValue().getParent(), is(parent));
    }
}
