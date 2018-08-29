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
import de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy;
import de.qaware.sdfx.windowmtg.api.JavaFXThreadingRule;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import javafx.scene.Parent;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

import static de.qaware.sdfx.windowmtg.impl.JavaFxTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for the {@link WindowManagerImpl}.
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class WindowManagerImplTest {

    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();
    @Mock
    private DragNDropManager dragNDropManager;
    @Mock
    private RootArea mainWindow;
    @Captor
    private ArgumentCaptor<ViewStatus> viewStatusCaptor;

    private Stage mainStage;
    private ViewStatus view1 = mockView("view1", "view1");
    private ViewStatus view2 = mockView("view2", "view2");
    private Map<String, ViewStatus> views;
    private List<RootArea> subWindows;

    @InjectMocks
    private WindowManagerImpl windowManager;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        mockReadOnlyProperty(windowManager, "mainRootArea", mainWindow);
        FieldUtils.writeField(windowManager, "dragNDropManager", dragNDropManager, true);
        views = getViewsStatus();
        subWindows = (List<RootArea>)FieldUtils.readField(windowManager, "subWindows", true);
        mainStage = mockStageForArea(mainWindow);
        when(dragNDropManager.getWindowManager()).thenReturn(windowManager);
        Lookup.init(new ServiceLoaderLookupStrategy());
    }

    @SuppressWarnings("unchecked")
    private Map<String, ViewStatus> getViewsStatus()  throws Exception {
        return (Map<String, ViewStatus>) FieldUtils.readField(windowManager, "viewsStatus", true);
    }

    @Test
    public void testInit() throws Exception {
        ViewStatus view1 = mock(ViewStatus.class);
        views.put("view1", view1);
        ViewStatus view2 = mock(ViewStatus.class);
        views.put("view2", view2);
        windowManager.init();
        verify(dragNDropManager).init();
        verify(view1).setDividerPositions();
        verify(view2).setDividerPositions();
    }

    /**
     * Test {@link WindowManagerImpl#register(View, boolean)} case successful registration of a view
     */
    @Test
    public void testRegister() throws Exception {
        ViewStatus view = mockView("newView", "new View");
        assertThat(views.size(), is(equalTo(0)));

        windowManager.register(view.getView());

        verify(mainWindow).add(viewStatusCaptor.capture(), any(Position.class));
        assertThat(viewStatusCaptor.getValue().getView(), is(equalTo(view.getView())));
        assertThat(views.size(), is(equalTo(1)));
        assertThat(windowManager.getRegisteredViews(), hasItem(view.getView()));
    }

    /**
     * Test {@link WindowManagerImpl#register(View, boolean)} case register a new view with already registered view id.
     */
    @Test
    public void testRegisterViewExists() throws Exception {
        views.put("view1", view1);
        views.put("view2", view2);
        TabArea targetArea = view2.getArea();
        when(targetArea.isValid()).thenReturn(true);
        ViewStatus view = mockView("view2", "new View");

        assertThat(views.size(), is(equalTo(2)));

        windowManager.register(view.getView());

        ViewStatus newViewStatus = views.get("view2");
        verify(targetArea).add(newViewStatus, Position.CENTER);
        verify(targetArea).remove(view2);
        assertThat(views.size(), is(equalTo(2)));
        assertThat(windowManager.getRegisteredViews(), hasItem(view.getView()));
    }

    /**
     * Test {@link WindowManagerImpl#register(View, View, boolean)} case successful and show the registered view.
     */
    @Test
    public void testRegisterParent() throws Exception {
        views.put("view2", view2);

        windowManager.register(view1.getView(), view2.getView());

        verify(view2.getArea()).add(viewStatusCaptor.capture(), any(Position.class));
        ViewStatus viewStatus = viewStatusCaptor.getValue();
        assertThat(views.size(), is(equalTo(2)));
        assertThat(views, hasEntry("view1", viewStatus));
        assertThat(windowManager.getRegisteredViews(), hasItem(view1.getView()));
    }

    /**
     * Test {@link WindowManagerImpl#register(View, View, boolean)} case successful but don't show the registered view.
     */
    @Test
    public void testRegisterParentDoNotShow() throws Exception {
        views.put("view2", view2);
        windowManager.register(view1.getView(), view2.getView(), false);
        verify(view2.getArea(), never()).add(any(), any(Position.class));
        assertThat(views, hasKey("view1"));
        assertThat(windowManager.getRegisteredViews(), hasItem(view1.getView()));
    }

    /**
     * Test {@link WindowManagerImpl#register(View, boolean)} case successful register an already registered but closed
     * view
     */
    @Test
    public void testReRegisterClosedView() throws Exception {
        views.put("view1", view1);
        view1.setArea(null);

        windowManager.register(view1.getView());

        verify(mainWindow).add(viewStatusCaptor.capture(), any(Position.class));
        assertThat(viewStatusCaptor.getValue().getView(), is(equalTo(view1.getView())));
        assertThat(views.size(), is(equalTo(1)));
        assertThat(windowManager.getRegisteredViews(), hasItem(view1.getView()));
    }

    /**
     * Test {@link WindowManagerImpl#register(View, boolean)} case successful register an already registered but closed
     * view and the view should not be visible after registration.
     */
    @Test
    public void testReRegisterClosedViewDontShow() throws Exception {
        views.put("view1", view1);
        view1.setArea(null);
        windowManager.register(view1.getView(), false);
        verify(mainWindow, never()).add(any(), any(Position.class));

        assertThat(views.size(), is(equalTo(1)));
        assertThat(views, hasKey("view1"));
        assertThat(windowManager.getRegisteredViews(), hasItem(view1.getView()));
    }

    /**
     * Test {@link WindowManagerImpl#register(View, View, boolean)} case successful register an already registered view
     */
    @Test
    public void testRegisterParentReRegistered() throws Exception {
        views.put("view1", view1);
        views.put("view2", view2);

        windowManager.register(view1.getView(), view2.getView());
        verify(view2.getArea()).add(viewStatusCaptor.capture(), any(Position.class));
        ViewStatus viewStatus = viewStatusCaptor.getValue();
        verify(view1.getArea()).remove(view1);

        assertThat(views.size(), is(equalTo(2)));
        assertThat(views, hasEntry("view1", viewStatus));
        assertThat(windowManager.getRegisteredViews(), hasItem(view1.getView()));
    }

    /**
     * Test {@link WindowManagerImpl#register(View, View, boolean)} case successful register view with parent. but
     * parent view is closed
     */
    @Test
    public void testRegisterParentClosed() throws Exception {
        views.put("view2", view2);
        view2.setArea(null);

        windowManager.register(view1.getView(), view2.getView());

        verify(mainWindow).add(viewStatusCaptor.capture(), any(Position.class));
        ViewStatus viewStatus = viewStatusCaptor.getValue();
        assertThat(views.size(), is(equalTo(2)));
        assertThat(views, hasEntry("view1", viewStatus));
        assertThat(windowManager.getRegisteredViews(), hasItem(view1.getView()));
    }

    /**
     * Test {@link WindowManagerImpl#register(View, boolean)} case view2 as parent is not registered.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRegisterParentNoParentFound() throws Exception {
        windowManager.register(view1.getView(), view2.getView());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUnregister() throws Exception {
        views.put(view1.getView().getViewId(), view1);
        views.put(view2.getView().getViewId(), view2);
        List<View> viewList = (List<View>) FieldUtils.readField(windowManager, "views", true);
        viewList.add(view1.getView());
        viewList.add(view2.getView());

        assertThat(windowManager.unregister(view2.getView()), is(true));
        assertThat(viewList, hasSize(1));
        assertThat(viewList, contains(view1.getView()));
        assertThat(views.size(), is(equalTo(1)));
    }

    @Test
    public void testGetRootPane() throws Exception {
        Parent parent = mock(Parent.class);
        when(mainWindow.getNode()).thenReturn(parent);
        assertThat(windowManager.getRootPane(), is(parent));
    }

    @Test
    public void testRestoreDefaultLayout() throws Exception {
        RootArea area = mock(RootArea.class);
        mockStageForArea(area);
        subWindows.add(area);
        views.put("view1", view1);
        views.put("view2", view2);
        FieldUtils.writeField(view2, "parent", view1, true);

        windowManager.restoreDefaultLayout();
        assertThat(subWindows, hasSize(0));

        views = getViewsStatus();
        assertThat(views.size(), is(equalTo(2)));
        assertThat(views.get("view1"), is(not(equalTo(view1))));
        assertThat(views.get("view1").getView(), is(equalTo(view1.getView())));
        assertThat(views.get("view1").getParent(), is(nullValue()));
        assertThat(views.get("view2"), is(not(equalTo(view2))));
        assertThat(views.get("view2").getView(), is(equalTo(view2.getView())));
        assertThat(views.get("view2").getParent(), is(equalTo(views.get("view1"))));

        assertThat(windowManager.getRegisteredViews(), hasSize(2));
        assertThat(windowManager.getRegisteredViews(), containsInAnyOrder(view1.getView(), view2.getView()));

        verify(views.get("view1").getView()).getViewAreaSize();
        verify(views.get("view2").getView()).getViewAreaSize();
    }

    @Test
    public void testCloseView() throws Exception {
        views.put("view1", view1);
        views.put("view2", view2);
        assertThat(windowManager.closeView(view1.getView()), is(true));
        assertThat(view1.getStatus(), is(ViewStatus.Status.HIDDEN));
    }

    @Test
    public void testCloseViewAlreadyClosed() throws Exception {
        views.put("view1", view1);
        view1.setArea(null);
        assertThat(windowManager.closeView(view1.getView()), is(false));
        assertThat(view1.getStatus(), is(ViewStatus.Status.HIDDEN));
    }

    @Test
    public void testCloseViewNotExists() throws Exception {
        assertThat(windowManager.closeView(mock(View.class)), is(false));
    }

    @Test
    public void testShowViewWithinTabPane() throws Exception {
        TabPane tabPane = new TabPane();
        views.put("view1", view1);
        views.put("view2", view2);
        view1.setStatus(ViewStatus.Status.VISIBLE);
        tabPane.getTabs().add(view2.getTab());
        tabPane.getTabs().add(view1.getTab());
        assertThat(tabPane.getSelectionModel().getSelectedItem(), is(equalTo(view2.getTab())));
        windowManager.showView(view1.getView());
        assertThat(tabPane.getSelectionModel().getSelectedItem(), is(equalTo(view1.getTab())));
        assertThat(windowManager.getFocusedView(), is(view1.getView()));
    }

    @Test
    public void testShowViewNoParent() throws Exception {
        views.put("view1", view1);
        views.put("view2", view2);
        view1.setStatus(ViewStatus.Status.HIDDEN);

        windowManager.showView(view1.getView());

        assertThat(views.size(), is(equalTo(2)));
        assertThat(views.get("view1"), is(not(equalTo(view1))));
        assertThat(views.get("view1").getView(), is(equalTo(view1.getView())));
        assertThat(views.get("view1").getParent(), is(nullValue()));
    }

    @Test
    public void testShowViewParents() throws Exception {
        views.put("view1", view1);
        views.put("view2", view2);
        ViewStatus view3 = mockView("view3", "view3");
        views.put("view3", view3);
        FieldUtils.writeField(view1, "parent", view2, true);
        FieldUtils.writeField(view2, "parent", view3, true);
        view2.setStatus(ViewStatus.Status.HIDDEN);
        view2.setArea(null);

        windowManager.showView(view1.getView());


        assertThat(views.size(), is(equalTo(3)));
        assertThat(views.get("view1"), is(not(equalTo(view1))));
        assertThat(views.get("view1").getView(), is(equalTo(view1.getView())));
        assertThat(views.get("view1").getParent(), is(view2));

        verify(view3.getArea()).add(viewStatusCaptor.capture(), any());
        assertThat(viewStatusCaptor.getValue(), is(views.get("view1")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShowViewUnregistered() throws Exception {
        windowManager.showView(view1.getView());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShowViewWrongView() throws Exception {
        views.put("view1", view2);
        windowManager.showView(view1.getView());
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
    public void testRegisterArea() throws Exception {
        RootArea rootArea1 = mock(RootArea.class);
        assertThat(subWindows, hasSize(0));
        windowManager.register(rootArea1);
        assertThat(subWindows, hasSize(1));
        assertThat(subWindows, contains(rootArea1));
    }

    @Test
    public void testBringToFront() throws Exception {
        RootArea rootArea1 = mock(RootArea.class);
        Stage stage1 = mockStageForArea(rootArea1);
        windowManager.register(rootArea1);
        windowManager.bringToFront();
        verify(mainStage).toFront();
        verify(stage1).toFront();
    }

    @Test
    public void testRemove() throws Exception {
        ViewStatus view3 = mockView("view3", "view3");
        views.put("view1", view1);
        views.put("view2", view2);
        views.put("view3", view3);

        RootArea rootArea1 = mock(RootArea.class);
        TabArea tabArea = mock(TabArea.class);
        when(rootArea1.getFirstChild()).thenReturn(tabArea);
        when(tabArea.getRootArea()).thenReturn(rootArea1);
        when(view2.getArea()).thenReturn(tabArea);
        when(view3.getArea()).thenReturn(tabArea);

        Stage stage = mockStageForArea(rootArea1);

        windowManager.remove(rootArea1);
        assertThat(views.size(), is(equalTo(3)));
        verify(view2.getArea()).remove(view2);
        verify(view3.getArea()).remove(view3);
        verify(stage).close();
    }

    @Test
    public void testGetMainRootArea() throws Exception {
        assertThat(windowManager.getMainRootArea(), is(mainWindow));
        mockReadOnlyProperty(windowManager, "mainRootArea", null);
        assertThat(windowManager.getMainRootArea(), is(notNullValue()));
    }

    @Test
    public void testRedrawAreas() throws Exception {
        RootArea rootArea1 = mock(RootArea.class);
        Parent mainParent = mock(Parent.class);
        Parent parent1 = mock(Parent.class);
        when(mainWindow.getNode()).thenReturn(mainParent);
        when(rootArea1.getNode()).thenReturn(parent1);
        windowManager.register(rootArea1);
        windowManager.redrawAreas();
        verify(mainWindow.getNode()).requestLayout();
        verify(rootArea1.getNode()).requestLayout();
    }

    @Test
    public void testGetVisibleViews() throws Exception {
        ViewStatus view3 = mockView("view3", "view3");
        ViewStatus view4 = mockView("view4", "view4");
        views.put("view1", view1);
        views.put("view2", view2);
        views.put("view3", view3);
        views.put("view4", view4);

        mockReadOnlyProperty(view1.getTab(), "selected", true);
        mockReadOnlyProperty(view4.getTab(), "selected", true);

        List<View> visibleViews = windowManager.getVisibleViews();
        assertThat(visibleViews, hasSize(2));
        assertThat(visibleViews, containsInAnyOrder(view1.getView(), view4.getView()));

        assertThat(windowManager.hasVisibleView(view1.getView()), is(true));
        assertThat(windowManager.hasVisibleView(view2.getView()), is(false));
    }

    @Test
    public void testHasRegisteredView() throws Exception {
        ViewStatus view3 = mockView("view3", "view3");
        ViewStatus view4 = mockView("view4", "view4");
        views.put(view1.getView().getViewId(), view1);
        views.put("view3", view4);

        assertThat(windowManager.hasRegisteredView(view1.getView()), is(true));
        assertThat(windowManager.hasRegisteredView(view2.getView()), is(false));
        assertThat(windowManager.hasRegisteredView(view3.getView()), is(false));
    }
}
