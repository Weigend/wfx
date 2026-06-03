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
package io.softwareecg.wfx.windowmanager.impl;

import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.lookup.api.LookupStrategy;
import io.softwareecg.wfx.windowmanager.testutil.JavaFXThreadingRule;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static io.softwareecg.wfx.windowmanager.impl.DragNDropManager.DATAFORMAT;
import static io.softwareecg.wfx.windowmanager.impl.JavaFxTestUtils.mockDragboard;
import static io.softwareecg.wfx.windowmanager.impl.JavaFxTestUtils.mockReadOnlyProperty;
import static io.softwareecg.wfx.windowmanager.impl.JavaFxTestUtils.mockView;
import static javafx.scene.input.TransferMode.COPY;
import static javafx.scene.input.TransferMode.MOVE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link DragNDropManagerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DragNDropManagerImplTest {
    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();

    @Mock
    private MultiWindowManager windowManager;

    @Mock
    private DropStage dropStage;

    @Mock
    private LookupStrategy lookupStrategy;

    @InjectMocks
    private DragNDropManagerImpl dragNDropManager;

    private final Pane rootPane = new Pane();
    private final Scene scene = new Scene(rootPane);

    @Before
    public void setUp() throws Exception {
        when(windowManager.getRootPane()).thenReturn(rootPane);
        FieldUtils.writeField(dragNDropManager, "dropStage", dropStage, true);
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(ViewContainerAreaFactory.class)).thenReturn(new ViewContainerAreaFactoryMockImpl());
        // Reset static state from previous tests
        DragNDropManagerImpl.setDraggedViewStatus(null);
    }

    @Test
    public void testInitInstallsDragExitedHandler() {
        dragNDropManager.init();
        DragEvent event = new DragEvent(null, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE,
                null, rootPane, null);
        EventHandler<? super DragEvent> onDragExited = scene.getOnDragExited();
        // Wrap to verify the consumed flag inside the handler chain.
        scene.setOnDragExited(dragEvent -> {
            onDragExited.handle(dragEvent);
            assertThat(dragEvent.isConsumed(), is(true));
        });
        rootPane.fireEvent(event);
    }

    @Test
    public void testOnDragDetectedNotWithinHeader() {
        // Drag detected on a TabPane but not within the tab header → no-op.
        TabPane tabPane = new TabPane();
        MouseEvent event = new MouseEvent(tabPane, tabPane, MouseEvent.DRAG_DETECTED, 1, 1, 0, 0,
                MouseButton.PRIMARY, 1, false, false, false, false, false, false, false, false, false, false,
                mock(PickResult.class));
        dragNDropManager.onDragDetected(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragDetectedWrongSource() {
        // Drag detected with non-TabPane source must be ignored without consuming.
        MouseEvent mouseEvent = mock(MouseEvent.class);
        when(mouseEvent.getSource()).thenReturn(new Pane());
        dragNDropManager.onDragDetected(mouseEvent);
        verify(mouseEvent, never()).consume();
    }

    @Test
    public void testOnDragDoneConsumesEvent() throws Exception {
        TabPane tabPane = new TabPane();
        tabPane.setUserData(mock(TabArea.class));
        DragEvent event = new DragEvent(tabPane, scene, DragEvent.DRAG_DONE, mockDragboard(DATAFORMAT, "abc"),
                0, 0, 0, 0, MOVE, null, rootPane, null);
        ViewStatus view = mock(ViewStatus.class);
        DragNDropManagerImpl.setDraggedViewStatus(view);
        dragNDropManager.onDragDone(event);
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragDoneInvalidDragBoardConsumesButDoesNotResize() throws Exception {
        // Drag boards with the wrong DataFormat are still consumed but should
        // not trigger any divider repositioning.
        TabPane tabPane = new TabPane();
        tabPane.setUserData(mock(TabArea.class));
        DragEvent event = new DragEvent(tabPane, scene, DragEvent.DRAG_DONE,
                mockDragboard(DataFormat.PLAIN_TEXT, "abc"), 0, 0, 0, 0, MOVE, null, rootPane, null);
        ViewStatus view = mock(ViewStatus.class);
        DragNDropManagerImpl.setDraggedViewStatus(view);
        dragNDropManager.onDragDone(event);
        verify(view, never()).setDividerPositions();
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragDoneCopyModeShrinksAndClearsDroppedStage() throws Exception {
        // For COPY drag mode with a previously-dropped Stage, that stage's
        // width is reduced by 1 (anti-flicker workaround) and the reference
        // is cleared.
        TabPane tabPane = new TabPane();
        tabPane.setUserData(mock(TabArea.class));
        DragEvent event = new DragEvent(tabPane, scene, DragEvent.DRAG_DONE, mockDragboard(DATAFORMAT, "abc"),
                0, 0, 0, 0, COPY, null, rootPane, null);

        Stage droppedStage = new Stage();
        mockReadOnlyProperty(droppedStage, "width", 10.0);
        FieldUtils.writeField(dragNDropManager, "droppedStage", droppedStage, true);
        dragNDropManager.onDragDone(event);
        assertThat(FieldUtils.readField(dragNDropManager, "droppedStage", true), is(nullValue()));
        assertThat(droppedStage.getWidth(), is(equalTo(9.0)));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragDoneWrongSource() {
        // Source not a TabPane → event is ignored without consuming.
        DragEvent event = new DragEvent(new Pane(), scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0,
                MOVE, null, rootPane, null);
        dragNDropManager.onDragDone(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragDoneWrongUserData() {
        // TabPane source but no TabArea user-data → event is ignored.
        TabPane source = new TabPane();
        source.setUserData(new Object());
        DragEvent event = new DragEvent(source, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0,
                MOVE, null, rootPane, null);
        dragNDropManager.onDragDone(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragExited() {
        // Use a real Pane: JavaFX Node carries a @IDProperty annotation that
        // Mockito's inline mockmaker cannot inspect across module boundaries.
        Node node = new Pane();
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE,
                null, rootPane, null);
        dragNDropManager.onDragExited(event);
        assertThat(node.getEffect(), is(nullValue()));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragExitedWrongSource() {
        DragEvent event = new DragEvent(new Object(), scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0,
                MOVE, null, rootPane, null);
        dragNDropManager.onDragExited(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragOver() throws Exception {
        Control control = new Label();
        control.setUserData(mock(ViewArea.class));
        mockReadOnlyProperty(control, "width", 10.0);
        mockReadOnlyProperty(control, "height", 10.0);
        DragEvent event = new DragEvent(control, scene, DragEvent.DRAG_EXITED, null, 1, 1, 0, 0, MOVE,
                null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(FieldUtils.readField(dragNDropManager, "effectTarget", true), is(sameInstance(control)));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragOverToInvalidCenter() throws Exception {
        // Drag over the centre of an area that does not allow centre drops:
        // no effect target is registered and no visual effect is applied.
        Control control = new Label();
        control.setUserData(mock(ViewArea.class));
        mockReadOnlyProperty(control, "width", 10.0);
        mockReadOnlyProperty(control, "height", 10.0);
        DragEvent event = new DragEvent(control, scene, DragEvent.DRAG_EXITED, null, 5, 5, 0, 0, MOVE,
                null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(FieldUtils.readField(dragNDropManager, "effectTarget", true), is(nullValue()));
        assertThat(control.getEffect(), is(nullValue()));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragOverToCenter() throws Exception {
        Control control = new Label();
        ViewArea viewArea = mock(ViewArea.class);
        when(viewArea.dropToCenter()).thenReturn(true);
        control.setUserData(viewArea);
        mockReadOnlyProperty(control, "width", 10.0);
        mockReadOnlyProperty(control, "height", 10.0);
        DragEvent event = new DragEvent(control, scene, DragEvent.DRAG_EXITED, null, 5, 5, 0, 0, MOVE,
                null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(FieldUtils.readField(dragNDropManager, "effectTarget", true), is(sameInstance(control)));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragOverExistingEffectTarget() throws Exception {
        Control control = new Label();
        control.setUserData(mock(ViewArea.class));
        mockReadOnlyProperty(control, "width", 10.0);
        mockReadOnlyProperty(control, "height", 10.0);
        FieldUtils.writeField(dragNDropManager, "effectTarget", control, true);
        DragEvent event = new DragEvent(control, scene, DragEvent.DRAG_EXITED, null, 1, 1, 0, 0, MOVE,
                null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(control.getEffect(), is(nullValue()));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragOverOtherEffectTarget() throws Exception {
        Control control = new Label();
        control.setUserData(mock(ViewArea.class));
        mockReadOnlyProperty(control, "width", 10.0);
        mockReadOnlyProperty(control, "height", 10.0);
        FieldUtils.writeField(dragNDropManager, "effectTarget", new Label(), true);
        DragEvent event = new DragEvent(control, scene, DragEvent.DRAG_EXITED, null, 8, 8, 0, 0, MOVE,
                null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(FieldUtils.readField(dragNDropManager, "effectTarget", true), is(sameInstance(control)));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragOverWrongSource() {
        DragEvent event = new DragEvent(new Object(), scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0,
                MOVE, null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragDroppedNewStageDragBoardNull() {
        Node node = new Pane();
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE,
                null, rootPane, null);
        dragNDropManager.onDragDroppedNewStage(event, new Stage());
        assertThat(event.isDropCompleted(), is(false));
    }

    @Test
    public void testOnDragDroppedNewStageDragBoardInvalid() throws Exception {
        Node node = new Pane();
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_DROPPED,
                mockDragboard(DataFormat.PLAIN_TEXT, "abc"), 0, 0, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragDroppedNewStage(event, new Stage());
        assertThat(event.isDropCompleted(), is(false));
    }

    @Test
    public void testOnDragDroppedWrongTarget() throws Exception {
        Control control = new Label();
        control.setUserData(new Object());
        DragEvent event = new DragEvent(new Pane(), scene, DragEvent.DRAG_DROPPED,
                mockDragboard(DATAFORMAT, "abc"), 0, 0, 0, 0, MOVE, null, control, null);
        DragNDropManagerImpl.setDraggedViewStatus(mockView("abc", "abc"));
        dragNDropManager.onDragDropped(event);
        assertThat(event.isDropCompleted(), is(false));
    }

    @Test
    public void testOnDragDroppedWrongGestureTarget() throws Exception {
        // Use a real Pane: JavaFX Node carries a @IDProperty annotation that
        // Mockito's inline mockmaker cannot inspect across module boundaries.
        Node node = new Pane();
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_DROPPED,
                mockDragboard(DATAFORMAT, "abc"), 0, 0, 0, 0, MOVE, null, rootPane, null);
        DragNDropManagerImpl.setDraggedViewStatus(mockView("abc", "abc"));
        dragNDropManager.onDragDropped(event);
        assertThat(event.isDropCompleted(), is(false));
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragDroppedDragBoardInvalid() throws Exception {
        // Use a real Pane: JavaFX Node carries a @IDProperty annotation that
        // Mockito's inline mockmaker cannot inspect across module boundaries.
        Node node = new Pane();
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_DROPPED,
                mockDragboard(DATAFORMAT, "abcd"), 0, 0, 0, 0, MOVE, null, rootPane, null);
        DragNDropManagerImpl.setDraggedViewStatus(mockView("abc", "abc"));
        dragNDropManager.onDragDropped(event);
        assertThat(event.isDropCompleted(), is(false));
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragDroppedDragBoardNull() {
        // Use a real Pane: JavaFX Node carries a @IDProperty annotation that
        // Mockito's inline mockmaker cannot inspect across module boundaries.
        Node node = new Pane();
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE,
                null, rootPane, null);
        dragNDropManager.onDragDropped(event);
        assertThat(event.isDropCompleted(), is(false));
        assertThat(event.isConsumed(), is(false));
    }
}
