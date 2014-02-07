// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 07.02.14 10:57
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.Position;
import javafx.scene.control.Label;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * Test the root area.
 */
@RunWith(MockitoJUnitRunner.class)
public class RootAreaTest {

    private RootArea rootArea;

    @Mock
    private ViewArea firstChild;

    @Mock
    private DragNDropManager dragNDropManager;

    @Mock
    private MultiWindowManager windowManager;

    @Before
    public void setUp() throws Exception {
        when(dragNDropManager.getWindowManager()).thenReturn(windowManager);
        when(firstChild.getNode()).thenReturn(new Label("abc"));
        rootArea = new RootArea(dragNDropManager, false);
        rootArea.setFirstChild(firstChild);
    }

    @Test
    public void testSetFirstChild() throws Exception {

    }


    @Test(expected = UnsupportedOperationException.class)
    public void testSetSecondChild() throws Exception {
        rootArea.setSecondChild(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSplit() throws Exception {
        rootArea.split(null, null, null);
    }

    @Test
    public void testAdd() throws Exception {
        ViewStatus status = mock(ViewStatus.class);
        rootArea.add(status, Position.CENTER);
        verify(firstChild).add(status, Position.CENTER);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() throws Exception {
        rootArea.remove(firstChild);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveCloseNoClose() throws Exception {
        rootArea.remove(firstChild);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetParent() throws Exception {
        rootArea.setParent(null);
    }
}
