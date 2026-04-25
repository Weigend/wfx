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
import io.softwareecg.wfx.lookup.LookupStrategy;
import io.softwareecg.wfx.windowmtg.api.JavaFXThreadingRule;
import io.softwareecg.wfx.windowmtg.api.Position;
import io.softwareecg.wfx.windowmtg.api.View;
import javafx.geometry.Orientation;
import javafx.scene.layout.Pane;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for the {@link ViewArea}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewAreaTest {
    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();

    @Mock
    private ViewArea parentArea;

    @Mock
    private TabArea editorArea;

    @Mock
    private ViewStatus initialEditorView;

    @Mock
    private TabArea secondArea;

    @Mock
    private DragNDropManager dragNDropManager;

    @Mock
    private MultiWindowManager windowManager;

    @Mock
    private LookupStrategy lookupStrategy;

    private ViewArea viewArea;

    @Before
    public void setUp() {
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(ViewContainerAreaFactory.class)).thenReturn(new ViewContainerAreaFactoryMockImpl());
        View v = new TestView("initial", Position.CENTER);

        when(dragNDropManager.getWindowManager()).thenReturn(windowManager);
        when(initialEditorView.getView()).thenReturn(v);
        when(editorArea.isEditor()).thenReturn(true);
        when(initialEditorView.getArea()).thenReturn(editorArea);
        // Use a real Pane rather than mock(Parent.class): JavaFX's Parent
        // carries a @IDProperty annotation that Mockito's inline mockmaker
        // cannot inspect across module boundaries.
        when(editorArea.getNode()).thenReturn(new Pane());

        viewArea = new ViewArea(dragNDropManager);
        viewArea.setFirstChild(editorArea);
        viewArea.setEditor(editorArea.isEditor());
        viewArea.setSecondChild(secondArea);
        viewArea.setParent(parentArea);
    }

    /** Reflectively set the private orientation field — required to simulate
     *  a viewArea that was already split in a given direction. */
    private void setOrientation(ViewArea area, Orientation orientation) throws Exception {
        Method m = ViewArea.class.getDeclaredMethod("setOrientation", Orientation.class);
        m.setAccessible(true);
        m.invoke(area, orientation);
    }

    @Test
    public void testAddCenter() {
        // CENTER must always go to the editor area, regardless of which child
        // it is in this viewArea.
        viewArea.add(initialEditorView, Position.CENTER);
        verify(editorArea).add(initialEditorView, Position.CENTER);
    }

    @Test
    public void testAddCenterEditorIsSecondChild() {
        // Same as above but with editor on the secondChild slot.
        viewArea.setFirstChild(secondArea);
        viewArea.setSecondChild(editorArea);
        viewArea.add(initialEditorView, Position.CENTER);
        verify(editorArea).add(initialEditorView, Position.CENTER);
    }

    @Test
    public void testAddTopOnUnsplitAreaCreatesVerticalSplit() {
        ArgumentCaptor<ViewArea> captor = ArgumentCaptor.forClass(ViewArea.class);
        viewArea.add(initialEditorView, Position.TOP);
        verify(parentArea).replace(eq(viewArea), captor.capture());

        // For TOP: new view becomes firstChild, viewArea becomes secondChild.
        assertThat(captor.getValue().getFirstChild().isEditor(), is(false));
        assertThat(captor.getValue().getSecondChild(), is(sameInstance(viewArea)));
        assertThat(captor.getValue().isEditor(), is(true));
        assertThat(captor.getValue().getParent(), is(sameInstance(parentArea)));
    }

    @Test
    public void testAddTopOnVerticalSplitRecursesIntoFirstChild() throws Exception {
        // Already vertically split → TOP goes into the top half (firstChild).
        setOrientation(viewArea, Orientation.VERTICAL);
        viewArea.add(initialEditorView, Position.TOP);
        verify(editorArea).add(initialEditorView, Position.TOP);
    }

    @Test
    public void testAddBottomOnUnsplitAreaCreatesVerticalSplit() {
        ArgumentCaptor<ViewArea> captor = ArgumentCaptor.forClass(ViewArea.class);
        viewArea.add(initialEditorView, Position.BOTTOM);
        verify(parentArea).replace(eq(viewArea), captor.capture());

        // For BOTTOM: viewArea stays as firstChild, new view becomes secondChild.
        assertThat(captor.getValue().getFirstChild(), is(sameInstance(viewArea)));
        assertThat(captor.getValue().getSecondChild().isEditor(), is(false));
        assertThat(captor.getValue().isEditor(), is(true));
        assertThat(captor.getValue().getParent(), is(sameInstance(parentArea)));
    }

    @Test
    public void testAddBottomOnVerticalSplitRecursesIntoSecondChild() throws Exception {
        setOrientation(viewArea, Orientation.VERTICAL);
        viewArea.add(initialEditorView, Position.BOTTOM);
        verify(secondArea).add(initialEditorView, Position.BOTTOM);
    }

    @Test
    public void testAddLeftOnUnsplitAreaCreatesHorizontalSplit() {
        ArgumentCaptor<ViewArea> captor = ArgumentCaptor.forClass(ViewArea.class);
        viewArea.add(initialEditorView, Position.LEFT);
        verify(parentArea).replace(eq(viewArea), captor.capture());

        // For LEFT: new view becomes firstChild, viewArea becomes secondChild.
        assertThat(captor.getValue().getSecondChild(), is(sameInstance(viewArea)));
        assertThat(captor.getValue().getFirstChild().isEditor(), is(false));
        assertThat(captor.getValue().isEditor(), is(true));
        assertThat(captor.getValue().getParent(), is(sameInstance(parentArea)));
    }

    @Test
    public void testAddLeftOnHorizontalSplitRecursesIntoFirstChild() throws Exception {
        // Bug fix verification: when viewArea is already horizontally split,
        // LEFT must recurse into the LEFT half (firstChild = editorArea), NOT
        // into the right half (secondChild = secondArea). Before the fix, the
        // LEFT case in ViewArea.add() called getSecondChild() — a copy-paste
        // leftover from RIGHT — so a later LEFT registration ended up inside
        // the existing right pane.
        setOrientation(viewArea, Orientation.HORIZONTAL);
        viewArea.add(initialEditorView, Position.LEFT);
        verify(editorArea).add(initialEditorView, Position.LEFT);
    }

    @Test
    public void testAddRightOnUnsplitAreaCreatesHorizontalSplit() {
        ArgumentCaptor<ViewArea> captor = ArgumentCaptor.forClass(ViewArea.class);
        viewArea.add(initialEditorView, Position.RIGHT);
        verify(parentArea).replace(eq(viewArea), captor.capture());

        // For RIGHT: viewArea stays as firstChild, new view becomes secondChild.
        assertThat(captor.getValue().getFirstChild(), is(sameInstance(viewArea)));
        assertThat(captor.getValue().getSecondChild().isEditor(), is(false));
        assertThat(captor.getValue().isEditor(), is(true));
        assertThat(captor.getValue().getParent(), is(sameInstance(parentArea)));
    }

    @Test
    public void testAddRightOnHorizontalSplitRecursesIntoSecondChild() throws Exception {
        setOrientation(viewArea, Orientation.HORIZONTAL);
        viewArea.add(initialEditorView, Position.RIGHT);
        verify(secondArea).add(initialEditorView, Position.RIGHT);
    }

    @Test
    public void testRemoveFirstChildPromotesSecondToParent() {
        ViewArea parent = new ViewArea(dragNDropManager);
        parent.setFirstChild(viewArea);
        viewArea.setParent(parent);
        viewArea.remove(editorArea);
        assertThat(parent.getFirstChild(), is(sameInstance((ViewArea) secondArea)));
        assertThat(parent.getSecondChild(), is(nullValue()));
    }

    @Test
    public void testRemoveSecondChildPromotesFirstToParent() {
        ViewArea parent = new ViewArea(dragNDropManager);
        parent.setSecondChild(viewArea);
        viewArea.setParent(parent);
        viewArea.remove(secondArea);
        assertThat(parent.getFirstChild(), is(nullValue()));
        assertThat(parent.getSecondChild(), is(sameInstance((ViewArea) editorArea)));
    }

    @Test
    public void testReplaceFirstChild() {
        viewArea.replace(editorArea, secondArea);
        assertThat(viewArea.getFirstChild(), is(sameInstance((ViewArea) secondArea)));
    }

    @Test
    public void testReplaceSecondChild() {
        viewArea.replace(secondArea, editorArea);
        assertThat(viewArea.getSecondChild(), is(sameInstance((ViewArea) editorArea)));
    }

    @Test
    public void testGetRootAreaWalksUpToRoot() {
        RootArea rootArea = new RootArea(dragNDropManager, false);
        when(parentArea.getParent()).thenReturn(rootArea);
        assertThat(viewArea.getRootArea(), is(sameInstance(rootArea)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSplitRejectsBothSidesEqualToThis() {
        // split(first, second, ...) requires either first==this OR second==this,
        // not both.
        viewArea.split(viewArea, viewArea, Orientation.HORIZONTAL);
    }

    @Test
    public void testIsValidWithNullParent() {
        viewArea.setParent(null);
        assertThat(viewArea.isValid(), is(false));
    }

    @Test
    public void testIsValidAsFirstChild() {
        ViewArea parent = new ViewArea(dragNDropManager);
        parent.setParent(mock(ViewArea.class));
        when(parent.getParent().isValid()).thenReturn(true);
        when(parent.getParent().getFirstChild()).thenReturn(parent);
        parent.setFirstChild(viewArea);
        viewArea.setParent(parent);
        assertThat(viewArea.isValid(), is(true));
    }

    @Test
    public void testIsValidAsFirstChildButParentInvalid() {
        ViewArea parent = new ViewArea(dragNDropManager);
        parent.setParent(mock(ViewArea.class));
        when(parent.getParent().isValid()).thenReturn(false);
        when(parent.getParent().getFirstChild()).thenReturn(parent);
        parent.setFirstChild(viewArea);
        viewArea.setParent(parent);
        assertThat(viewArea.isValid(), is(false));
    }

    @Test
    public void testIsValidAsSecondChild() {
        ViewArea parent = new ViewArea(dragNDropManager);
        parent.setParent(mock(ViewArea.class));
        when(parent.getParent().isValid()).thenReturn(true);
        when(parent.getParent().getFirstChild()).thenReturn(parent);
        parent.setSecondChild(viewArea);
        viewArea.setParent(parent);
        assertThat(viewArea.isValid(), is(true));
    }

    @Test
    public void testIsInvalidWhenNotInParent() {
        ViewArea parent = new ViewArea(dragNDropManager);
        parent.setFirstChild(mock(ViewArea.class));
        parent.setSecondChild(mock(ViewArea.class));
        viewArea.setParent(parent);
        assertThat(viewArea.isValid(), is(false));
    }
}
