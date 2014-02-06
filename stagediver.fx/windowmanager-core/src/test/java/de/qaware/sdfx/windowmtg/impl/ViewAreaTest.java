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

import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import javafx.scene.Parent;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Created by christian.fritz on 06.02.14.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewAreaTest {

    private ViewArea viewArea;

    @Mock
    private TabArea editorArea;

    @Mock
    private ViewStatus initialEditorAreaStatus;

    @Mock
    private TabArea secondArea;

    @Mock
    private DragNDropManager dragNDropManager;

    @Before
    public void setUp() throws Exception {
        viewArea = new ViewArea(dragNDropManager);
        viewArea.setFirstChild(editorArea);
        viewArea.setSecondChild(secondArea);

        View v = mock(View.class);
        when(v.getViewId()).thenReturn("initialEditor");

        when(initialEditorAreaStatus.getView()).thenReturn(v);
        when(editorArea.isEditor()).thenReturn(true);
        when(initialEditorAreaStatus.getArea()).thenReturn(editorArea);
        when(editorArea.getNode()).thenReturn(mock(Parent.class));
    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void testAddCenter() throws Exception {
        viewArea.add(initialEditorAreaStatus, Position.CENTER);
        verify(editorArea).add(initialEditorAreaStatus, Position.CENTER);
    }

    @Test
    @Ignore
    public void testRemove() throws Exception {

    }

    @Test
    @Ignore
    public void testReplace() throws Exception {

    }

    @Test
    @Ignore
    public void testGetRootArea() throws Exception {

    }

    @Test
    @Ignore
    public void testDropToCenter() throws Exception {

    }
}
