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

import io.softwareecg.wfx.windowmtg.api.JavaFXThreadingRule;
import io.softwareecg.wfx.windowmtg.api.Position;
import io.softwareecg.wfx.windowmtg.api.View;
import javafx.scene.control.SplitPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Test for {@link ViewStatus}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewStatusTest {
    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();

    @Mock
    private View view;

    @Mock
    private ViewArea parentArea;

    @Mock
    private SplitPane parentNode;

    private ViewStatus status;

    @Before
    public void setUp() {
        when(view.getDefaultPosition()).thenReturn(Position.LEFT);
        when(view.getViewAreaSize()).thenReturn(0.25);
        when(parentArea.getNode()).thenReturn(parentNode);
    }

    @Test
    public void testRestoreDefault() {
        status = new ViewStatus(view);
        status.setPosition(Position.CENTER);
        status.setStatus(ViewStatus.Status.HIDDEN);
        status.restoreDefault();
        assertThat(status.getPosition(), is(equalTo(view.getDefaultPosition())));
        assertThat(status.getStatus(), is(ViewStatus.Status.VISIBLE));
    }

    @Test
    public void testSetDividerPositionsLeft() {
        when(view.getDefaultPosition()).thenReturn(Position.LEFT);
        status = new ViewStatus(view);
        status.setArea(mockTabArea());
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode).setDividerPositions(0.25);
    }

    @Test
    public void testSetDividerPositionsBottom() {
        when(view.getDefaultPosition()).thenReturn(Position.BOTTOM);
        status = new ViewStatus(view);
        status.setArea(mockTabArea());
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode).setDividerPositions(0.75);
    }

    @Test
    public void testSetDividerPositionsTooSmall() {
        when(view.getViewAreaSize()).thenReturn(0.01);
        status = new ViewStatus(view);
        status.setArea(mockTabArea());
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode, never()).setDividerPositions(anyDouble());
    }

    @Test
    public void testSetDividerPositionsTooBig() {
        when(view.getViewAreaSize()).thenReturn(0.99);
        status = new ViewStatus(view);
        status.setArea(mockTabArea());
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode, never()).setDividerPositions(anyDouble());
    }

    @Test
    public void testSetDividerPositionsNoSplitpane() {
        // If the parent's node is not a SplitPane, setDividerPositions must do nothing
        // and must not interact with that non-split node.
        when(view.getViewAreaSize()).thenReturn(0.25);
        GridPane gridPane = mock(GridPane.class);
        when(parentArea.getNode()).thenReturn(gridPane);
        status = new ViewStatus(view);
        status.setArea(mockTabArea());
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode, never()).setDividerPositions(anyDouble());
        verifyNoInteractions(gridPane);
    }

    @Test
    public void testSetDividerPositionsForCenterPosition() {
        // CENTER falls into the default switch branch and only logs; no divider call.
        when(view.getViewAreaSize()).thenReturn(0.5);
        when(view.getDefaultPosition()).thenReturn(Position.CENTER);
        status = new ViewStatus(view);
        status.setArea(mockTabArea());
        when(status.getArea().getParent()).thenReturn(parentArea);
        status.setDividerPositions();
        verify(parentNode, never()).setDividerPositions(anyDouble());
    }

    @Test
    public void testTabImage() {
        when(view.getViewImagePath()).thenReturn(getClass().getResource("/io/softwareecg/wfx/windowmtg/api/test-icon.png"));
        status = new ViewStatus(view);
        assertThat(((ImageView) status.getTab().getGraphic()).getImage(), is(notNullValue()));
    }

    private TabArea mockTabArea() {
        TabArea area = mock(TabArea.class);
        when(area.isValid()).thenReturn(true);
        return area;
    }
}
