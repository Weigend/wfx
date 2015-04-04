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
import javafx.scene.layout.*;

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
    public void testSetDividerPositionsLeft() throws Exception {
        when(view.getDefaultPosition()).thenReturn(Position.LEFT);
        status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode).setDividerPositions(0.25);
    }

    @Test
    public void testSetDividerPositionsBottom() throws Exception {
        when(view.getDefaultPosition()).thenReturn(Position.BOTTOM);
        status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode).setDividerPositions(0.75);
    }

    @Test
    public void testSetDividerPositionsToSmall() throws Exception {
        when(view.getViewAreaSize()).thenReturn(0.01);
        status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode, never()).setDividerPositions(anyDouble());
    }

    @Test
    public void testSetDividerPositionsToBig() throws Exception {
        when(view.getViewAreaSize()).thenReturn(0.99);
        status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode, never()).setDividerPositions(anyDouble());
    }

    @Test
    public void testSetDividerPositionsNoSplitpane() throws Exception {
        when(view.getViewAreaSize()).thenReturn(0.01);
        GridPane gridPane = mock(GridPane.class);
        when(parentArea.getNode()).thenReturn(gridPane);
        status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode, never()).setDividerPositions(anyDouble());
        verifyZeroInteractions(gridPane);
    }

    @Test
    public void testSetDividerPositionsForCenterPosition() throws Exception {
        when(view.getViewAreaSize()).thenReturn(0.5);
        when(view.getDefaultPosition()).thenReturn(Position.CENTER);
        status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode, never()).setDividerPositions(anyDouble());
    }
}
