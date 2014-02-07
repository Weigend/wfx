// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 06.02.14 15:51
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import com.google.inject.AbstractModule;
import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test for the view area.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewAreaTest {

    private ViewArea viewArea;

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

    @BeforeClass
    public static void setUpClass() throws Exception {
        Lookup.init(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ViewConainterAreaFactory.class).to(ViewConainterAreaFactoryMockImpl.class).asEagerSingleton();
            }
        });
    }

    @Before
    public void setUp() throws Exception {
        View v = new TestView("initial", Position.CENTER);

        when(dragNDropManager.getWindowManager()).thenReturn(windowManager);
        when(initialEditorView.getView()).thenReturn(v);
        when(editorArea.isEditor()).thenReturn(true);
        when(initialEditorView.getArea()).thenReturn(editorArea);
        when(editorArea.getNode()).thenReturn(mock(Parent.class));

        viewArea = new ViewArea(dragNDropManager);
        viewArea.setFirstChild(editorArea);
        viewArea.setEditor(editorArea.isEditor());
        viewArea.setSecondChild(secondArea);
        viewArea.setParent(parentArea);
    }

    @Test
    public void testAddCenter() throws Exception {
        viewArea.add(initialEditorView, Position.CENTER);
        verify(editorArea).add(initialEditorView, Position.CENTER);
    }

    @Test
    public void testAddCenterSecondChild() throws Exception {
        viewArea.setFirstChild(secondArea);
        viewArea.setSecondChild(editorArea);
        viewArea.add(initialEditorView, Position.CENTER);
        verify(editorArea).add(initialEditorView, Position.CENTER);
    }

    @Test
    public void testAddTopSplit() throws Exception {
        ArgumentCaptor<ViewArea> viewAreaCaptor = ArgumentCaptor.forClass(ViewArea.class);

        when(initialEditorView.getTab()).thenCallRealMethod();
        viewArea.add(initialEditorView, Position.TOP);
        verify(parentArea).replace(eq(viewArea), viewAreaCaptor.capture());

        assertThat(viewAreaCaptor.getValue().getFirstChild().isEditor(), is(false));
        assertThat(viewAreaCaptor.getValue().getSecondChild(), is(viewArea));
        assertThat(viewAreaCaptor.getValue().isEditor(), is(true));
        assertThat(viewAreaCaptor.getValue().getParent(), is(parentArea));
    }

    @Test
    public void testAddBottomSplit() throws Exception {
        ArgumentCaptor<ViewArea> viewAreaCaptor = ArgumentCaptor.forClass(ViewArea.class);

        when(initialEditorView.getTab()).thenCallRealMethod();
        viewArea.add(initialEditorView, Position.BOTTOM);
        verify(parentArea).replace(eq(viewArea), viewAreaCaptor.capture());

        assertThat(viewAreaCaptor.getValue().getFirstChild(), is(viewArea));
        assertThat(viewAreaCaptor.getValue().getSecondChild().isEditor(), is(false));
        assertThat(viewAreaCaptor.getValue().isEditor(), is(true));
        assertThat(viewAreaCaptor.getValue().getParent(), is(parentArea));
    }

    @Test
    public void testAddLeftSplit() throws Exception {
        ArgumentCaptor<ViewArea> viewAreaCaptor = ArgumentCaptor.forClass(ViewArea.class);

        when(initialEditorView.getTab()).thenCallRealMethod();
        viewArea.add(initialEditorView, Position.LEFT);
        verify(parentArea).replace(eq(viewArea), viewAreaCaptor.capture());

        assertThat(viewAreaCaptor.getValue().getSecondChild(), is(viewArea));
        assertThat(viewAreaCaptor.getValue().getFirstChild().isEditor(), is(false));
        assertThat(viewAreaCaptor.getValue().isEditor(), is(true));
        assertThat(viewAreaCaptor.getValue().getParent(), is(parentArea));
    }

    @Test
    public void testAddRightSplit() throws Exception {
        ArgumentCaptor<ViewArea> viewAreaCaptor = ArgumentCaptor.forClass(ViewArea.class);

        when(initialEditorView.getTab()).thenCallRealMethod();
        viewArea.add(initialEditorView, Position.RIGHT);
        verify(parentArea).replace(eq(viewArea), viewAreaCaptor.capture());

        assertThat(viewAreaCaptor.getValue().getFirstChild(), is(viewArea));
        assertThat(viewAreaCaptor.getValue().getSecondChild().isEditor(), is(false));
        assertThat(viewAreaCaptor.getValue().isEditor(), is(true));
        assertThat(viewAreaCaptor.getValue().getParent(), is(parentArea));
    }

    @Test
    public void testRemoveFirst() throws Exception {
        ViewArea parent = new ViewArea(dragNDropManager);
        parent.setFirstChild(viewArea);
        viewArea.setParent(parent);
        viewArea.remove(editorArea);
        assertThat(parent.getFirstChild(), is((ViewArea) secondArea));
        assertThat(parent.getSecondChild(), is(nullValue()));
    }

    @Test
    public void testRemoveSecond() throws Exception {
        ViewArea parent = new ViewArea(dragNDropManager);
        parent.setSecondChild(viewArea);
        viewArea.setParent(parent);
        viewArea.remove(secondArea);
        assertThat(parent.getFirstChild(), is(nullValue()));
        assertThat(parent.getSecondChild(), is((ViewArea) editorArea));
    }

    @Test
    public void testReplaceFirst() throws Exception {
        viewArea.replace(editorArea, secondArea);
        assertThat(viewArea.getFirstChild(), is((ViewArea) secondArea));
    }

    @Test
    public void testReplaceSecond() throws Exception {
        viewArea.replace(secondArea, editorArea);
        assertThat(viewArea.getSecondChild(), is((ViewArea) editorArea));
    }


    @Test
    public void testGetRootArea() throws Exception {
        RootArea rootArea = new RootArea(dragNDropManager, false);
        when(parentArea.getParent()).thenReturn(rootArea);
        assertThat(viewArea.getRootArea(), is(rootArea));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSplitFail() throws Exception {
        viewArea.split(viewArea, viewArea, Orientation.HORIZONTAL);
    }
}
