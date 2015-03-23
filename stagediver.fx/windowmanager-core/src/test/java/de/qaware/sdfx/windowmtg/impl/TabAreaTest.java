// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 07.02.14 11:23
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.windowmtg.api.Position;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test for tab area.
 */
@RunWith(MockitoJUnitRunner.class)
public class TabAreaTest {

    private TabArea tabArea;

    @Mock
    private DragNDropManager dragNDropManager;

    @Mock
    private MultiWindowManager windowManager;

    private RootArea rootArea = new RootArea(dragNDropManager, true);

    @Mock
    private ObservableList<Tab> tabs;

    @Mock
    private ViewArea parent;

    @Mock
    private LookupStrategy lookupStrategy;


    @Before
    public void setUp() throws Exception {
        Lookup.init(lookupStrategy);
        when(dragNDropManager.getWindowManager()).thenReturn(windowManager);
        when(parent.getParent()).thenReturn(rootArea);

        //todo: replace tabs list
        tabArea = new TabArea(parent, dragNDropManager);
        TabPane tabPane = mock(TabPane.class);

        //tabs = new SimpleListProperty<>();
        Field tabsField = TabPane.class.getDeclaredField("tabs");
        tabsField.setAccessible(true);
        tabsField.set(tabPane, tabs);

        Field tabPaneField = TabArea.class.getDeclaredField("tabPane");
        tabPaneField.setAccessible(true);
        tabPaneField.set(tabArea, tabPane);
        when(lookupStrategy.lookup(ViewContainerAreaFactory.class)).thenReturn(new ViewContainerAreaFactoryMockImpl());
    }

    @Test
    public void testRemoveViewNotAssigned() throws Exception {
        ViewStatus view = mock(ViewStatus.class);
        tabArea.remove(view);
        verify(view, never()).setArea(null);
    }

    @Test
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
    }

    @Test
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
    }

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
