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
package io.softwareecg.wfx.windowmtg.impl;

import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.lookup.serviceloader.ServiceLoaderLookupStrategy;
import io.softwareecg.wfx.windowmtg.testutil.JavaFXThreadingRule;
import io.softwareecg.wfx.windowmtg.api.Position;
import io.softwareecg.wfx.windowmtg.api.View;
import io.softwareecg.wfx.windowmtg.api.ViewKind;
import javafx.scene.Parent;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
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
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.softwareecg.wfx.windowmtg.impl.JavaFxTestUtils.mockReadOnlyProperty;
import static io.softwareecg.wfx.windowmtg.impl.JavaFxTestUtils.mockStageForArea;
import static io.softwareecg.wfx.windowmtg.impl.JavaFxTestUtils.mockView;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link WindowManagerImpl}.
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

    @InjectMocks
    private WindowManagerImpl windowManager;

    private Stage mainStage;
    private final ViewStatus view1 = mockView("view1", "view1");
    private final ViewStatus view2 = mockView("view2", "view2");
    private Map<String, ViewStatus> views;
    private List<RootArea> subWindows;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        mockReadOnlyProperty(windowManager, "mainRootArea", mainWindow);
        FieldUtils.writeField(windowManager, "dragNDropManager", dragNDropManager, true);
        views = (Map<String, ViewStatus>) FieldUtils.readField(windowManager, "viewsStatus", true);
        subWindows = (List<RootArea>) FieldUtils.readField(windowManager, "subWindows", true);
        mainStage = mockStageForArea(mainWindow);
        when(dragNDropManager.getWindowManager()).thenReturn(windowManager);
        Lookup.init(new ServiceLoaderLookupStrategy());

        // The real WindowManagerImpl.register() chain calls
        // setDividerPositions() on every freshly created ViewStatus, which
        // dereferences viewStatus.getArea().getParent().getNode(). When mainWindow
        // is a Mockito mock, mainWindow.add(viewStatus, position) is a no-op and
        // never assigns an area to the new ViewStatus → NPE. Stub the mock to
        // attach a plausible TabArea so divider math can run end-to-end.
        doAnswer(invocation -> {
            ViewStatus vs = invocation.getArgument(0);
            TabArea area = mock(TabArea.class);
            ViewArea parent = mock(ViewArea.class);
            when(area.getParent()).thenReturn(parent);
            when(parent.getNode()).thenReturn(new Pane());
            vs.setArea(area);
            return null;
        }).when(mainWindow).add(any(ViewStatus.class), any(Position.class));
    }

    @Test
    public void testInit() {
        // The setDividerPositions sweep skips views without an attached area
        // (closed / detached views), so make these mocks "attached".
        ViewStatus v1 = mock(ViewStatus.class);
        when(v1.getArea()).thenReturn(mock(TabArea.class));
        views.put("view1", v1);
        ViewStatus v2 = mock(ViewStatus.class);
        when(v2.getArea()).thenReturn(mock(TabArea.class));
        views.put("view2", v2);
        windowManager.init();
        verify(dragNDropManager).init();
        verify(v1).setDividerPositions();
        verify(v2).setDividerPositions();
    }

    @Test
    public void testRegister() {
        ViewStatus view = mockView("newView", "new View");
        assertThat(views.size(), is(equalTo(0)));

        windowManager.register(view.getView());

        verify(mainWindow).add(viewStatusCaptor.capture(), any(Position.class));
        assertThat(viewStatusCaptor.getValue().getView(), is(equalTo(view.getView())));
        assertThat(views.size(), is(equalTo(1)));
        assertThat(windowManager.getRegisteredViews(), hasItem(view.getView()));
    }

    @Test
    public void testRegisterViewExists() {
        // Re-registering a view id targets the same TabArea: the old view is
        // removed, the new one added in CENTER position. The map size stays the same.
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

    @Test
    public void testRegisterParent() {
        views.put("view2", view2);

        windowManager.register(view1.getView(), view2.getView());

        verify(view2.getArea()).add(viewStatusCaptor.capture(), any(Position.class));
        ViewStatus viewStatus = viewStatusCaptor.getValue();
        assertThat(views.size(), is(equalTo(2)));
        assertThat(views, hasEntry("view1", viewStatus));
        assertThat(windowManager.getRegisteredViews(), hasItem(view1.getView()));
    }

    @Test
    public void testRegisterParentDoNotShow() {
        views.put("view2", view2);
        windowManager.register(view1.getView(), view2.getView(), false);
        verify(view2.getArea(), never()).add(any(), any(Position.class));
        assertThat(views, hasKey("view1"));
        assertThat(windowManager.getRegisteredViews(), hasItem(view1.getView()));
    }

    @Test
    public void testReRegisterClosedView() {
        views.put("view1", view1);
        view1.setArea(null);

        windowManager.register(view1.getView());

        verify(mainWindow).add(viewStatusCaptor.capture(), any(Position.class));
        assertThat(viewStatusCaptor.getValue().getView(), is(equalTo(view1.getView())));
        assertThat(views.size(), is(equalTo(1)));
        assertThat(windowManager.getRegisteredViews(), hasItem(view1.getView()));
    }

    @Test
    public void testReRegisterClosedViewDontShow() {
        views.put("view1", view1);
        view1.setArea(null);
        windowManager.register(view1.getView(), false);
        verify(mainWindow, never()).add(any(), any(Position.class));

        assertThat(views.size(), is(equalTo(1)));
        assertThat(views, hasKey("view1"));
        assertThat(windowManager.getRegisteredViews(), hasItem(view1.getView()));
    }

    @Test
    public void testRegisterParentReRegistered() {
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

    @Test
    public void testRegisterParentClosed() {
        // When the parent view's area is null (parent was closed), the new
        // view falls back to mainWindow as the registration target.
        views.put("view2", view2);
        view2.setArea(null);

        windowManager.register(view1.getView(), view2.getView());

        verify(mainWindow).add(viewStatusCaptor.capture(), any(Position.class));
        ViewStatus viewStatus = viewStatusCaptor.getValue();
        assertThat(views.size(), is(equalTo(2)));
        assertThat(views, hasEntry("view1", viewStatus));
        assertThat(windowManager.getRegisteredViews(), hasItem(view1.getView()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterParentNotFound() {
        // No parent registered → IllegalArgumentException.
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
    public void testGetRootPane() {
        Parent parent = new Pane();
        when(mainWindow.getNode()).thenReturn(parent);
        assertThat(windowManager.getRootPane(), is(sameInstance(parent)));
    }

    @Test
    public void testRestoreDefaultLayout() throws Exception {
        // After restoring, the views map is rebuilt: subWindows are cleared,
        // fresh ViewStatus instances are created (so the original ViewStatus
        // references are no longer in viewsStatus), and the parent linkage is
        // preserved by reference to the original View objects.
        // Use the public register API so the tool registry is populated —
        // restoreDefaultLayout works off that registry, not viewsStatus.
        RootArea area = mock(RootArea.class);
        mockStageForArea(area);
        subWindows.add(area);
        windowManager.register(view1.getView());
        windowManager.register(view2.getView(), view1.getView());

        windowManager.restoreDefaultLayout();
        assertThat(subWindows, hasSize(0));

        @SuppressWarnings("unchecked")
        Map<String, ViewStatus> reloaded = (Map<String, ViewStatus>) FieldUtils.readField(windowManager, "viewsStatus", true);
        assertThat(reloaded.size(), is(equalTo(2)));
        assertThat(reloaded.get("view1").getView(), is(equalTo(view1.getView())));
        assertThat(reloaded.get("view1").getParent(), is(nullValue()));
        assertThat(reloaded.get("view2").getView(), is(equalTo(view2.getView())));
        assertThat(reloaded.get("view2").getParent(), is(equalTo(reloaded.get("view1"))));

        assertThat(windowManager.getRegisteredViews(), hasSize(2));
        assertThat(windowManager.getRegisteredViews(), containsInAnyOrder(view1.getView(), view2.getView()));

        // restoreDefaultLayout re-registers each view, which triggers
        // setDividerPositions multiple times — only assert "at least once".
        verify(reloaded.get("view1").getView(), org.mockito.Mockito.atLeastOnce()).getViewAreaSize();
        verify(reloaded.get("view2").getView(), org.mockito.Mockito.atLeastOnce()).getViewAreaSize();
    }

    @Test
    public void testCloseView() {
        views.put("view1", view1);
        views.put("view2", view2);
        assertThat(windowManager.closeView(view1.getView()), is(true));
        assertThat(view1.getStatus(), is(ViewStatus.Status.HIDDEN));
    }

    @Test
    public void testCloseViewAlreadyClosed() {
        views.put("view1", view1);
        view1.setArea(null);
        assertThat(windowManager.closeView(view1.getView()), is(false));
        assertThat(view1.getStatus(), is(ViewStatus.Status.HIDDEN));
    }

    @Test
    public void testCloseViewNotExists() {
        assertThat(windowManager.closeView(mock(View.class)), is(false));
    }

    @Test
    public void testShowViewWithinTabPane() {
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
    public void testShowViewNoParent() {
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
        // Showing a view with a hidden chain of parents must re-register the
        // closest ancestor that has a still-valid area.
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
    public void testShowViewUnregistered() {
        windowManager.showView(view1.getView());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShowViewWrongView() {
        // The viewId points to a different ViewStatus than the View we ask to
        // show — the manager rejects that mismatch.
        views.put("view1", view2);
        windowManager.showView(view1.getView());
    }

    @Test
    public void testFindView() {
        views.put("view1", view1);
        assertThat(windowManager.findView("view1"), is(view1.getView()));
    }

    @Test
    public void testFindViewNotFound() {
        assertThat(windowManager.findView("not found"), is(nullValue()));
    }

    @Test
    public void testRegisterFocusesNewView() {
        // register(view, true) must transfer the logical focus onto the new view.
        // JavaFX's focusOwner only moves on user interaction, so without this
        // consumers would have to call setFocusedView() explicitly to get
        // focus-driven side panels to rebind.
        ViewStatus initial = mockView("initial", "initial");
        windowManager.register(initial.getView());
        assertThat(windowManager.getFocusedView(), is(initial.getView()));

        ViewStatus next = mockView("next", "next");
        windowManager.register(next.getView());
        assertThat(windowManager.getFocusedView(), is(next.getView()));
        assertThat(windowManager.getLastFocusedView(), is(initial.getView()));
    }

    @Test
    public void testRegisterDoNotShowDoesNotChangeFocus() {
        // register(view, false) must not steal focus — the view is queued, not displayed.
        ViewStatus initial = mockView("initial", "initial");
        windowManager.register(initial.getView());
        ViewStatus hidden = mockView("hidden", "hidden");

        windowManager.register(hidden.getView(), false);

        assertThat(windowManager.getFocusedView(), is(initial.getView()));
    }

    @Test
    public void testRegisterParentFocusesNewView() {
        // register(view, parent) routes through the same private register path,
        // so the new view must also become focused.
        views.put("view2", view2);
        windowManager.setFocusedView(view2.getView());

        windowManager.register(view1.getView(), view2.getView());

        assertThat(windowManager.getFocusedView(), is(view1.getView()));
        assertThat(windowManager.getLastFocusedView(), is(view2.getView()));
    }

    @Test
    public void testRegisterPublishesViewBeforeFocusEvents() {
        // TabArea's selectedItemProperty listener fires setFocusedView() from
        // INSIDE viewArea.add() when the tab pane was previously empty. At that
        // moment, focus listeners (e.g. side panels) routinely look the view
        // up via findView() / getRegisteredViews(). The registry must be
        // visible before the focus event fires, otherwise the listener sees a
        // half-registered view and skips its bind. Simulate that callback
        // pattern with a mainWindow.add() stub that calls setFocusedView from
        // within add(), then assert the registry-visible invariant.
        org.mockito.Mockito.doAnswer(invocation -> {
            ViewStatus vs = invocation.getArgument(0);
            TabArea area = mock(TabArea.class);
            ViewArea parent = mock(ViewArea.class);
            when(area.getParent()).thenReturn(parent);
            when(parent.getNode()).thenReturn(new Pane());
            vs.setArea(area);
            windowManager.setFocusedView(vs.getView());
            return null;
        }).when(mainWindow).add(any(ViewStatus.class), any(Position.class));

        ViewStatus newView = mockView("newView", "new");
        AtomicReference<View> focusedAtFireTime = new AtomicReference<>();
        AtomicBoolean registeredAtFireTime = new AtomicBoolean(false);
        AtomicBoolean inListAtFireTime = new AtomicBoolean(false);

        windowManager.focusedViewProperty().addListener((obs, old, focused) -> {
            if (focused == newView.getView()) {
                focusedAtFireTime.set(focused);
                registeredAtFireTime.set(windowManager.findView("newView") != null);
                inListAtFireTime.set(windowManager.getRegisteredViews().contains(newView.getView()));
            }
        });

        windowManager.register(newView.getView());

        assertThat("focusedViewProperty must fire for the new view",
                focusedAtFireTime.get(), is(newView.getView()));
        assertThat("findView must locate the new view when focusedViewProperty fires",
                registeredAtFireTime.get(), is(true));
        assertThat("getRegisteredViews() must contain the new view when focusedViewProperty fires",
                inListAtFireTime.get(), is(true));
    }

    @Test
    public void testSetFocusedViewIdempotent() {
        // Double-invocation with the same view must not corrupt lastFocusedView.
        // Without the idempotence guard, the second call would push the
        // already-current view into lastFocusedView and the actual previous
        // focus would be lost.
        View v1 = mock(View.class);
        View v2 = mock(View.class);
        windowManager.setFocusedView(v1);
        windowManager.setFocusedView(v2);
        windowManager.setFocusedView(v2);
        assertThat(windowManager.getFocusedView(), is(v2));
        assertThat(windowManager.getLastFocusedView(), is(v1));
    }

    @Test
    public void testRestoreDefaultLayoutPreservesFocus() throws Exception {
        // restoreDefaultLayout re-registers every view; without the suppression
        // flag the focus would surf through the chain and end on whichever view
        // happens to be last in iteration order.
        RootArea area = mock(RootArea.class);
        mockStageForArea(area);
        subWindows.add(area);
        views.put("view1", view1);
        views.put("view2", view2);
        windowManager.setFocusedView(view1.getView());

        windowManager.restoreDefaultLayout();

        assertThat(windowManager.getFocusedView(), is(view1.getView()));
    }

    @Test
    public void testSetGetFocusedView() {
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
    public void testRegisterArea() {
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
        // Resetting the property forces a re-creation on next access.
        mockReadOnlyProperty(windowManager, "mainRootArea", null);
        assertThat(windowManager.getMainRootArea(), is(notNullValue()));
    }

    @Test
    public void testRedrawAreas() {
        RootArea rootArea1 = mock(RootArea.class);
        Parent mainParent = new Pane();
        Parent parent1 = new Pane();
        when(mainWindow.getNode()).thenReturn(mainParent);
        when(rootArea1.getNode()).thenReturn(parent1);
        windowManager.register(rootArea1);
        windowManager.redrawAreas();
        // requestLayout is final on Parent — assert visible side effect through
        // having both nodes touched at least once. Here we re-verify the wiring.
        verify(mainWindow).getNode();
        verify(rootArea1).getNode();
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
    public void testHasRegisteredView() {
        ViewStatus view3 = mockView("view3", "view3");
        ViewStatus view4 = mockView("view4", "view4");
        views.put(view1.getView().getViewId(), view1);
        views.put("view3", view4);

        assertThat(windowManager.hasRegisteredView(view1.getView()), is(true));
        assertThat(windowManager.hasRegisteredView(view2.getView()), is(false));
        assertThat(windowManager.hasRegisteredView(view3.getView()), is(false));
    }

    // ---------------------------------------------------------------------
    // ViewKind / TOOL vs DOCUMENT lifecycle
    // ---------------------------------------------------------------------

    @Test
    public void testCloseToolHidesButKeepsRegistration() {
        // TOOLs are persistent panels — closing the tab hides them but
        // leaves the registration alone so the View menu stays stable and
        // showView() / restoreDefaultLayout can bring them back.
        ViewStatus tool = mockView("tool", "tool");
        windowManager.register(tool.getView());
        assertThat(windowManager.hasRegisteredView(tool.getView()), is(true));

        windowManager.closeView(tool.getView());

        assertThat(windowManager.hasRegisteredView(tool.getView()), is(true));
        assertThat(windowManager.getToolViews(), hasItem(tool.getView()));
    }

    @Test
    public void testCloseDocumentUnregisters() {
        // DOCUMENTs are transient — closing fully drops them from the
        // registry. The owning module is responsible for opening fresh ones
        // when the user asks again. Without this, closing chart tabs would
        // accumulate ghost ViewStatus instances.
        ViewStatus doc = mockView("doc", "doc");
        when(doc.getView().getKind()).thenReturn(ViewKind.DOCUMENT);
        windowManager.register(doc.getView());
        assertThat(windowManager.hasRegisteredView(doc.getView()), is(true));

        windowManager.closeView(doc.getView());

        assertThat(windowManager.hasRegisteredView(doc.getView()), is(false));
        assertThat(windowManager.getToolViews(), not(hasItem(doc.getView())));
    }

    @Test
    public void testTabCloseHandlerUnregistersDocument() {
        // The X button on a tab triggers ViewStatus's own onClosed handler,
        // which for DOCUMENTs must additionally call back into unregister
        // — otherwise the tab vanishes visually but the registration leaks.
        ViewStatus doc = mockView("doc", "doc");
        when(doc.getView().getKind()).thenReturn(ViewKind.DOCUMENT);
        windowManager.register(doc.getView());

        // Simulate the JavaFX tab-close gesture: the registered ViewStatus
        // is the one inside viewsStatus, not the test fixture's `doc`.
        ViewStatus registered = views.get("doc");
        registered.getTab().getOnClosed().handle(null);

        assertThat(windowManager.hasRegisteredView(doc.getView()), is(false));
    }

    @Test
    public void testTabCloseHandlerKeepsToolRegistered() {
        // TOOL: tab-X must NOT unregister — that would erase the entry
        // from the View menu. The default ViewStatus handler sets HIDDEN
        // and stops there.
        ViewStatus tool = mockView("tool", "tool");
        windowManager.register(tool.getView());

        ViewStatus registered = views.get("tool");
        registered.getTab().getOnClosed().handle(null);

        assertThat(windowManager.hasRegisteredView(tool.getView()), is(true));
        assertThat(windowManager.getToolViews(), hasItem(tool.getView()));
    }

    @Test
    public void testGetToolViewsReflectsInsertionOrder() {
        ViewStatus a = mockView("a", "a");
        ViewStatus b = mockView("b", "b");
        ViewStatus c = mockView("c", "c");
        windowManager.register(a.getView());
        windowManager.register(b.getView());
        windowManager.register(c.getView());

        assertThat(windowManager.getToolViews(),
                contains(a.getView(), b.getView(), c.getView()));
    }

    @Test
    public void testGetToolViewsExcludesDocuments() {
        ViewStatus tool = mockView("tool", "tool");
        ViewStatus doc = mockView("doc", "doc");
        when(doc.getView().getKind()).thenReturn(ViewKind.DOCUMENT);
        windowManager.register(tool.getView());
        windowManager.register(doc.getView());

        assertThat(windowManager.getToolViews(), contains(tool.getView()));
        assertThat(windowManager.getToolViews(), not(hasItem(doc.getView())));
    }

    @Test
    public void testReRegisteringSameToolDoesNotDuplicate() {
        // Re-register (same id) hits putIfAbsent in the tool registry —
        // first registration wins, subsequent ones are no-ops on the list.
        // This matters for showView() round-trips and for restoreDefaultLayout
        // which re-registers via the public path.
        ViewStatus tool = mockView("tool", "tool");
        windowManager.register(tool.getView());
        assertThat(windowManager.getToolViews(), hasSize(1));

        windowManager.register(tool.getView());

        assertThat(windowManager.getToolViews(), hasSize(1));
    }

    @Test
    public void testUnregisterToolEvictsFromToolRegistry() {
        // closeView on a TOOL hides; unregister is the explicit "gone for
        // good" path that must also strip the entry from the auto View menu.
        ViewStatus tool = mockView("tool", "tool");
        windowManager.register(tool.getView());
        assertThat(windowManager.getToolViews(), hasItem(tool.getView()));

        windowManager.unregister(tool.getView());

        assertThat(windowManager.getToolViews(), not(hasItem(tool.getView())));
    }

    @Test
    public void testRestoreDefaultLayoutDropsDocumentsKeepsTools() {
        ViewStatus tool = mockView("tool", "tool");
        ViewStatus doc = mockView("doc", "doc");
        when(doc.getView().getKind()).thenReturn(ViewKind.DOCUMENT);
        windowManager.register(tool.getView());
        windowManager.register(doc.getView());

        windowManager.restoreDefaultLayout();

        assertThat(windowManager.findView("tool"), is(notNullValue()));
        assertThat(windowManager.findView("doc"), is(nullValue()));
    }

    @Test
    public void testRestoreDefaultLayoutReopensClosedTool() {
        // The user closed a TOOL (so it's HIDDEN, gone from the canvas).
        // restoreDefaultLayout must put it back — that is the whole point
        // of the action.
        ViewStatus tool = mockView("tool", "tool");
        windowManager.register(tool.getView());
        windowManager.closeView(tool.getView());

        windowManager.restoreDefaultLayout();

        assertThat(windowManager.findView("tool"), is(notNullValue()));
    }
}
