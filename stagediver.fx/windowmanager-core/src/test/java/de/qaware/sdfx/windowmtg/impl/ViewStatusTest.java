// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 10.02.14 16:02
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.JavaFXThreadingRule;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javafx.scene.control.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * test for {@link ViewStatus}
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewStatusTest {
    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();
    private ViewStatus status;

    @Mock
    private View view;

    @Mock
    private ViewArea parentArea;

    @Mock
    private SplitPane parentNode;

    @Before
    public void setUp() throws Exception {
        when(view.getDefaultPosition()).thenReturn(Position.LEFT);
        when(view.getViewAreaSize()).thenReturn(0.25);
        when(parentArea.getNode()).thenReturn(parentNode);
    }


    @Test
    public void testRestoreDefault() throws Exception {
        status = new ViewStatus(view);
        status.setPosition(Position.CENTER);
        status.setStatus(ViewStatus.Status.HIDDEN);
        status.restoreDefault();
        assertThat(status.getPosition(), is(equalTo(view.getDefaultPosition())));
        assertThat(status.getStatus(), is(ViewStatus.Status.VISIBLE));
    }

    @Test
    public void testSetDeviderPositionsLeft() throws Exception {
        when(view.getDefaultPosition()).thenReturn(Position.LEFT);
        status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDeviderPositions();
        verify(parentNode).setDividerPositions(0.25);
    }

    @Test
    public void testSetDeviderPositionsBottom() throws Exception {
        when(view.getDefaultPosition()).thenReturn(Position.BOTTOM);
        status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDeviderPositions();
        verify(parentNode).setDividerPositions(0.75);
    }

    @Test
    public void testSetDeviderPositionsToSmall() throws Exception {
        when(view.getViewAreaSize()).thenReturn(0.01);
        status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDeviderPositions();
        verify(parentNode, never()).setDividerPositions(anyDouble());
    }

    @Test
    public void testSetDeviderPositionsToBig() throws Exception {
        when(view.getViewAreaSize()).thenReturn(0.99);
        status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDeviderPositions();
        verify(parentNode, never()).setDividerPositions(anyDouble());
    }
}
