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
import de.qaware.sdfx.windowmtg.api.JavaFXThreadingRule;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

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

    private Stage mainStage;
    private ViewStatus view1 = mockView("view1", "view1");
    private ViewStatus view2 = mockView("view2", "view2");

    @InjectMocks
    private WindowManagerImpl windowManager;
    private Map<String, ViewStatus> views;
    private List<RootArea> subWindows;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        Whitebox.setInternalState(windowManager, "dragNDropManager", dragNDropManager);
        views = (Map<String, ViewStatus>) Whitebox.getInternalState(windowManager, "views");
        subWindows = (List<RootArea>) Whitebox.getInternalState(windowManager, "subWindows");
        mainStage = mockStageForArea(mainWindow);
        when(dragNDropManager.getWindowManager()).thenReturn(windowManager);
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
        verify(view1).setDividerPositions();
        verify(view2).setDividerPositions();
    }

    @Test
    public void testRegisterViewWithoutParent() throws Exception {
        ViewStatus view = mockView("newView", "new View");
        ArgumentCaptor<ViewStatus> captor = ArgumentCaptor.forClass(ViewStatus.class);
        assertThat(views.size(), is(equalTo(0)));
        windowManager.register(view.getView());

        verify(mainWindow).add(captor.capture(), any(Position.class));
        assertThat(captor.getValue().getView(), is(equalTo(view.getView())));
        assertThat(views.size(), is(equalTo(1)));
    }

    @Test
    public void testRegisterViewWithoutParentViewExists() throws Exception {
        views.put("view1", view1);
        views.put("view2", view2);
        TabArea targetArea = view2.getArea();
        ViewStatus view = mockView("view2", "new View");

        assertThat(views.size(), is(equalTo(2)));

        windowManager.register(view.getView());

        ViewStatus newViewStatus = views.get("view2");
        verify(targetArea).add(newViewStatus, Position.CENTER);
        verify(targetArea).remove(view2);
        assertThat(views.size(), is(equalTo(2)));
    }

    @Test
    public void testRegisterParent() throws Exception {
        views.put("view2", view2);
        windowManager.register(view1.getView(), view2.getView());
        ArgumentCaptor<ViewStatus> captor = ArgumentCaptor.forClass(ViewStatus.class);
        verify(view2.getArea()).add(captor.capture(), any(Position.class));
        ViewStatus viewStatus = captor.getValue();
        assertThat(views.size(), is(equalTo(2)));
        assertThat(views, hasEntry("view1", viewStatus));
    }

    @Test
    public void testRegisterParentUnregistered() throws Exception {
        views.put("view1", view1);
        views.put("view2", view2);
        windowManager.register(view1.getView(), view2.getView());
        ArgumentCaptor<ViewStatus> captor = ArgumentCaptor.forClass(ViewStatus.class);
        verify(view2.getArea()).add(captor.capture(), any(Position.class));
        ViewStatus viewStatus = captor.getValue();
        verify(view1.getArea()).remove(view1);

        assertThat(views.size(), is(equalTo(2)));
        assertThat(views, hasEntry("view1", viewStatus));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterParentNoParentFound() throws Exception {
        windowManager.register(view1.getView(), view2.getView());
    }

    @Test
    public void testGetRootPane() throws Exception {
        Parent parent = mock(Parent.class);
        when(mainWindow.getNode()).thenReturn(parent);
        assertThat(windowManager.getRootPane(), is(parent));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRestoreDefaultLayout() throws Exception {
        RootArea area = mock(RootArea.class);
        mockStageForArea(area);
        subWindows.add(area);
        views.put("view1", view1);
        views.put("view2", view2);
        Whitebox.setInternalState(view2, "parent", view1);

        windowManager.restoreDefaultLayout();
        assertThat(subWindows, hasSize(0));

        views = (Map<String, ViewStatus>) Whitebox.getInternalState(windowManager, "views");
        assertThat(views.size(), is(equalTo(2)));
        assertThat(views.get("view1"), is(not(equalTo(view1))));
        assertThat(views.get("view1").getView(), is(equalTo(view1.getView())));
        assertThat(views.get("view1").getParent(), is(nullValue()));
        assertThat(views.get("view2"), is(not(equalTo(view2))));
        assertThat(views.get("view2").getView(), is(equalTo(view2.getView())));
        assertThat(views.get("view2").getParent(), is(equalTo(views.get("view1"))));
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
        Whitebox.setInternalState(view1, "parent", view2);
        Whitebox.setInternalState(view2, "parent", view3);
        view2.setStatus(ViewStatus.Status.HIDDEN);

        windowManager.showView(view1.getView());

        assertThat(views.size(), is(equalTo(3)));
        assertThat(views.get("view1"), is(not(equalTo(view1))));
        assertThat(views.get("view1").getView(), is(equalTo(view1.getView())));
        assertThat(views.get("view1").getParent(), is(view3));
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
        Whitebox.setInternalState(windowManager, "mainArea", null);
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

        view1.getTab().selectedProperty();
        view4.getTab().selectedProperty();
        Whitebox.setInternalState(Whitebox.getInternalState(view1.getTab(), "selected"), "value", true);
        Whitebox.setInternalState(Whitebox.getInternalState(view4.getTab(), "selected"), "value", true);

        List<View> visibleViews = windowManager.getVisibleViews();
        assertThat(visibleViews, hasSize(2));
        assertThat(visibleViews, containsInAnyOrder(view1.getView(), view4.getView()));
    }

    @SuppressWarnings("unchecked")
    public static Stage mockStageForArea(ViewArea area) {
        Parent parent = new Label();
        Scene scene = new Scene(parent);
        when(area.getNode()).thenReturn(parent);

        Stage stage = mock(Stage.class);
        scene.windowProperty();
        ReadOnlyObjectWrapper<Stage> stageProperty = (ReadOnlyObjectWrapper<Stage>) Whitebox.getInternalState(scene, "window");
        Whitebox.setInternalState(stageProperty, "value", stage);
        return stage;
    }

    public static ViewStatus mockView(String id, String title) {
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
