//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-core
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 24.03.15 20:33
//      description:
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import javafx.scene.*;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class WindowManagerImplTest {

    @Mock
    private DragNDropManager dragNDropManager;

    @Mock
    private RootArea mainWindow;

    private ViewStatus view1 = mockView("view1", "view1");
    private ViewStatus view2 = mockView("view2", "view2");

    @InjectMocks
    private WindowManagerImpl windowManager;
    private Map<String, ViewStatus> views;

    @Before
    public void setUp() throws Exception {
        Whitebox.setInternalState(windowManager, "dragNDropManager", dragNDropManager);
        views = (Map<String, ViewStatus>) Whitebox.getInternalState(windowManager, "views");
        Lookup.init(new ServiceLoaderLookupStrategy());
    }

    @Test
    public void testInit() throws Exception {
        ViewStatus view1 = mock(ViewStatus.class);
        views.put("view1", view1);
        ViewStatus view2 = mock(ViewStatus.class);
        views.put("view2", view2);
        windowManager.init();
        verify(dragNDropManager).init();
        verify(view1).setDeviderPositions();
        verify(view2).setDeviderPositions();
    }

    @Test
    public void testGetRootPane() throws Exception {
        Parent parent = mock(Parent.class);
        when(mainWindow.getNode()).thenReturn(parent);
        assertThat(windowManager.getRootPane(), is(parent));
    }

    @Test
    public void testCloseView() throws Exception {
        views.put("view1", view1);
        views.put("view2", view2);
        windowManager.closeView(view1.getView());
        assertThat(view1.getStatus(), is(ViewStatus.Status.HIDDEN));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCloseViewNotExists() throws Exception {
        windowManager.closeView(mock(View.class));
        assertThat(view1.getStatus(), is(ViewStatus.Status.HIDDEN));
    }

    @Test
    public void testFindView() throws Exception {
        views.put("view1", view1);
        assertThat(windowManager.findView("view1"), is(view1.getView()));
    }

    @Test
    public void testFindViewNotFound() throws Exception {
        assertThat(windowManager.findView("not found"), is(nullValue()));
    }

    @Test
    public void testSetGetFocusedView() throws Exception {
        assertThat(windowManager.getFocusedView(), is(nullValue()));
        assertThat(windowManager.getLastFocusedView(), is(nullValue()));
        View v1 = mock(View.class);
        View v2 = mock(View.class);
        windowManager.setFocusedView(v1);
        assertThat(windowManager.getFocusedView(), is(v1));
        assertThat(windowManager.getLastFocusedView(), is(nullValue()));
        windowManager.setFocusedView(v2);
        assertThat(windowManager.getFocusedView(), is(v2));
        assertThat(windowManager.getLastFocusedView(), is(v1));
    }

    @Test
    public void testGetMainRootArea() throws Exception {
        assertThat(windowManager.getMainRootArea(), is(mainWindow));
        Whitebox.setInternalState(windowManager, "mainArea", null);
        assertThat(windowManager.getMainRootArea(), is(notNullValue()));
    }

    private ViewStatus mockView(String id, String title) {
        View view = mock(View.class);
        when(view.getViewId()).thenReturn(id);
        when(view.getTitle()).thenReturn(title);
        when(view.getDefaultPosition()).thenReturn(Position.CENTER);
        when(view.getRootNode()).thenReturn(mock(Parent.class));
        ViewStatus status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
        return spy(status);
    }
}
