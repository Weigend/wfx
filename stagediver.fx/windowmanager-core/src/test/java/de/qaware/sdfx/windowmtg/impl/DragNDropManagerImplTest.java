package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.windowmtg.api.JavaFXThreadingRule;
import de.qaware.sdfx.windowmtg.api.Position;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static de.qaware.sdfx.windowmtg.impl.DragNDropManager.DATAFORMAT;
import static de.qaware.sdfx.windowmtg.impl.DragNDropManagerImpl.getDragedViewStatus;
import static de.qaware.sdfx.windowmtg.impl.DragNDropManagerImpl.setDragedViewStatus;
import static de.qaware.sdfx.windowmtg.impl.JavaFxTestUtils.*;
import static javafx.scene.input.TransferMode.COPY;
import static javafx.scene.input.TransferMode.MOVE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for the {@link DragNDropManagerImpl}.
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class DragNDropManagerImplTest {

    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();

    @Mock
    private MultiWindowManager windowManager;

    private Pane rootPane = new Pane();
    private Scene scene = new Scene(rootPane);

    @Mock
    private DropStage dropStage;
    @Mock
    private LookupStrategy lookupStrategy;

    @InjectMocks
    private DragNDropManagerImpl dragNDropManager;

    @Before
    public void setUp() throws Exception {
        when(windowManager.getRootPane()).thenReturn(rootPane);
        Whitebox.setInternalState(dragNDropManager, "dropStage", dropStage);
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(ViewContainerAreaFactory.class)).thenReturn(new ViewContainerAreaFactoryMockImpl());
    }

    @Test
    public void testInit() throws Exception {
        dragNDropManager.init();
        DragEvent event = new DragEvent(null, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE, null, rootPane, null);
        EventHandler<? super DragEvent> onDragExited = scene.getOnDragExited();
        scene.setOnDragExited(dragEvent -> {
            onDragExited.handle(dragEvent);
            assertThat(dragEvent.isConsumed(), is(true));
        });
        rootPane.fireEvent(event);
    }

    @Test
    public void testOnDragDetectedWrongSource() throws Exception {
        MouseEvent mouseEvent = mock(MouseEvent.class);
        when(mouseEvent.getSource()).thenReturn(new Pane());
        dragNDropManager.onDragDetected(mouseEvent);
        verify(mouseEvent, never()).consume();
    }

    @Test
    public void testOnDragDone() throws Exception {
        TabPane tabPane = new TabPane();
        tabPane.setUserData(mock(TabArea.class));
        DragEvent event = new DragEvent(tabPane, scene, DragEvent.DRAG_DONE, mockDragboard(DATAFORMAT, "abc"), 0, 0, 0, 0, MOVE, null, rootPane, null);
        ViewStatus view = mock(ViewStatus.class);
        setDragedViewStatus(view);
        dragNDropManager.onDragDone(event);

        verify(view).setDividerPositions();
        verify(dropStage).close();
        assertThat(Whitebox.getInternalState(dragNDropManager, "dropStage"), is(nullValue()));
        assertThat(event.isConsumed(), is(true));
        assertThat(getDragedViewStatus(), is(nullValue()));
    }

    @Test
    public void testOnDragDoneInvalidDragBoard() throws Exception {
        TabPane tabPane = new TabPane();
        tabPane.setUserData(mock(TabArea.class));
        DragEvent event = new DragEvent(tabPane, scene, DragEvent.DRAG_DONE, mockDragboard(DataFormat.PLAIN_TEXT, "abc"), 0, 0, 0, 0, MOVE, null, rootPane, null);
        ViewStatus view = mock(ViewStatus.class);
        setDragedViewStatus(view);
        dragNDropManager.onDragDone(event);
        verify(view, never()).setDividerPositions();
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragDoneWrongModeWithDroppedStage() throws Exception {
        TabPane tabPane = new TabPane();
        tabPane.setUserData(mock(TabArea.class));
        DragEvent event = new DragEvent(tabPane, scene, DragEvent.DRAG_DONE, mockDragboard(DATAFORMAT, "abc"), 0, 0, 0, 0, COPY, null, rootPane, null);

        Stage droppedStage = new Stage();
        mockReadOnlyProperty(droppedStage, "width", 10);
        Whitebox.setInternalState(dragNDropManager, "droppedStage", droppedStage);
        dragNDropManager.onDragDone(event);
        assertThat(Whitebox.getInternalState(dragNDropManager, "droppedStage"), is(nullValue()));
        assertThat(droppedStage.getWidth(), is(equalTo(9.0)));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragDoneWrongSource() throws Exception {
        DragEvent event = new DragEvent(new Pane(), scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragDone(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragDoneWrongUserData() throws Exception {
        TabPane source = new TabPane();
        source.setUserData(new Object());
        DragEvent event = new DragEvent(source, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragDone(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragExited() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragExited(event);
        assertThat(node.getEffect(), is(nullValue()));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragExitedWrongSource() throws Exception {
        DragEvent event = new DragEvent(new Object(), scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragExited(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragOver() throws Exception {
        Control control = new Label();
        control.setUserData(mock(ViewArea.class));
        mockReadOnlyProperty(control, "width", 10);
        mockReadOnlyProperty(control, "height", 10);
        DragEvent event = new DragEvent(control, scene, DragEvent.DRAG_EXITED, null, 1, 1, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(Whitebox.getInternalState(dragNDropManager, "effectTarget"), is(control));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragOverToInvalidCenter() throws Exception {
        Control control = new Label();
        control.setUserData(mock(ViewArea.class));
        mockReadOnlyProperty(control, "width", 10);
        mockReadOnlyProperty(control, "height", 10);
        DragEvent event = new DragEvent(control, scene, DragEvent.DRAG_EXITED, null, 5, 5, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(Whitebox.getInternalState(dragNDropManager, "effectTarget"), is(nullValue()));
        assertThat(control.getEffect(), is(nullValue()));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragOverToCenter() throws Exception {
        Control control = new Label();
        ViewArea viewArea = mock(ViewArea.class);
        when(viewArea.dropToCenter()).thenReturn(true);
        control.setUserData(viewArea);
        mockReadOnlyProperty(control, "width", 10);
        mockReadOnlyProperty(control, "height", 10);
        DragEvent event = new DragEvent(control, scene, DragEvent.DRAG_EXITED, null, 5, 5, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(Whitebox.getInternalState(dragNDropManager, "effectTarget"), is(control));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragOverExistingEffectTarget() throws Exception {
        Control control = new Label();
        control.setUserData(mock(ViewArea.class));
        mockReadOnlyProperty(control, "width", 10);
        mockReadOnlyProperty(control, "height", 10);
        Whitebox.setInternalState(dragNDropManager, "effectTarget", control);
        DragEvent event = new DragEvent(control, scene, DragEvent.DRAG_EXITED, null, 1, 1, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(control.getEffect(), is(nullValue()));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragOverOtherEffectTarget() throws Exception {
        Control control = new Label();
        control.setUserData(mock(ViewArea.class));
        mockReadOnlyProperty(control, "width", 10);
        mockReadOnlyProperty(control, "height", 10);
        Whitebox.setInternalState(dragNDropManager, "effectTarget", mock(Control.class));
        DragEvent event = new DragEvent(control, scene, DragEvent.DRAG_EXITED, null, 8, 8, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(Whitebox.getInternalState(dragNDropManager, "effectTarget"), is(control));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragOverWrongSource() throws Exception {
        DragEvent event = new DragEvent(new Object(), scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragDroppedNewStageDragBoardNull() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE, null, rootPane, null);
        Stage stage = mock(Stage.class);
        dragNDropManager.onDragDroppedNewStage(event, stage);
        assertThat(event.isDropCompleted(), is(false));
    }

    @Test
    public void testOnDragDroppedNewStage() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_DROPPED, mockDragboard(DragNDropManager.DATAFORMAT, "abc"), 0, 0, 0, 0, MOVE, null, rootPane, null);
        Stage stage = new Stage();
        setDragedViewStatus(mockView("abc", "abc"));
        mockReadOnlyProperty(stage, "width", 10);
        mockReadOnlyProperty(stage, "height", 10);
        dragNDropManager.onDragDroppedNewStage(event, stage);
        assertThat(event.isDropCompleted(), is(true));
    }

    @Test
    public void testOnDragDroppedNewStageDragBoardInvalid() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_DROPPED, mockDragboard(DataFormat.PLAIN_TEXT, "abc"), 0, 0, 0, 0, MOVE, null, rootPane, null);
        Stage stage = mock(Stage.class);
        dragNDropManager.onDragDroppedNewStage(event, stage);
        assertThat(event.isDropCompleted(), is(false));
    }

    @Test
    public void testOnDragDropped() throws Exception {
        Control control = new Label();
        ViewStatus view = mockView("abc", "abc");
        ViewArea target = mock(ViewArea.class);
        control.setUserData(target);
        mockReadOnlyProperty(control, "width", 10);
        mockReadOnlyProperty(control, "height", 10);
        DragEvent event = new DragEvent(mock(Node.class), scene, DragEvent.DRAG_DROPPED, mockDragboard(DragNDropManager.DATAFORMAT, "abc"), 5, 5, 0, 0, MOVE, null, control, null);
        setDragedViewStatus(view);
        Whitebox.setInternalState(dragNDropManager, "effectTarget", control);
        dragNDropManager.onDragDropped(event);
        verify(target).add(view, Position.CENTER);
        assertThat(event.isDropCompleted(), is(true));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragDroppedTop() throws Exception {
        Control control = new Label();
        ViewStatus view = mockView("abc", "abc");
        ViewArea target = mock(ViewArea.class);
        control.setUserData(target);
        mockReadOnlyProperty(control, "width", 10);
        mockReadOnlyProperty(control, "height", 10);
        setDragedViewStatus(view);
        DragEvent event = new DragEvent(mock(Node.class), scene, DragEvent.DRAG_DROPPED, mockDragboard(DragNDropManager.DATAFORMAT, "abc"), 5, 2, 0, 0, MOVE, null, control, null);
        dragNDropManager.onDragDropped(event);
        verify(target).add(view, Position.TOP);
    }

    @Test
    public void testOnDragDroppedBottom() throws Exception {
        Control control = new Label();
        ViewStatus view = mockView("abc", "abc");
        ViewArea target = mock(ViewArea.class);
        control.setUserData(target);
        mockReadOnlyProperty(control, "width", 10);
        mockReadOnlyProperty(control, "height", 10);
        setDragedViewStatus(view);
        DragEvent event = new DragEvent(mock(Node.class), scene, DragEvent.DRAG_DROPPED, mockDragboard(DragNDropManager.DATAFORMAT, "abc"), 5, 8, 0, 0, MOVE, null, control, null);
        dragNDropManager.onDragDropped(event);
        verify(target).add(view, Position.BOTTOM);
    }

    @Test
    public void testOnDragDroppedRight() throws Exception {
        Control control = new Label();
        ViewStatus view = mockView("abc", "abc");
        ViewArea target = mock(ViewArea.class);
        control.setUserData(target);
        mockReadOnlyProperty(control, "width", 10);
        mockReadOnlyProperty(control, "height", 10);
        setDragedViewStatus(view);
        DragEvent event = new DragEvent(mock(Node.class), scene, DragEvent.DRAG_DROPPED, mockDragboard(DragNDropManager.DATAFORMAT, "abc"), 8, 5, 0, 0, MOVE, null, control, null);
        dragNDropManager.onDragDropped(event);
        verify(target).add(view, Position.RIGHT);
    }

    @Test
    public void testOnDragDroppedLeft() throws Exception {
        Control control = new Label();
        ViewStatus view = mockView("abc", "abc");
        ViewArea target = mock(ViewArea.class);
        control.setUserData(target);
        mockReadOnlyProperty(control, "width", 10);
        mockReadOnlyProperty(control, "height", 10);
        setDragedViewStatus(view);
        DragEvent event = new DragEvent(mock(Node.class), scene, DragEvent.DRAG_DROPPED, mockDragboard(DragNDropManager.DATAFORMAT, "abc"), 2, 5, 0, 0, MOVE, null, control, null);
        dragNDropManager.onDragDropped(event);
        verify(target).add(view, Position.LEFT);
    }

    @Test
    public void testOnDragDroppedWrongTarget() throws Exception {
        Control control = mock(Control.class);
        ViewStatus view = mockView("abc", "abc");
        when(control.getUserData()).thenReturn(mock(Object.class));
        DragEvent event = new DragEvent(mock(Node.class), scene, DragEvent.DRAG_DROPPED, mockDragboard(DragNDropManager.DATAFORMAT, "abc"), 0, 0, 0, 0, MOVE, null, control, null);
        setDragedViewStatus(view);
        dragNDropManager.onDragDropped(event);
        assertThat(event.isDropCompleted(), is(false));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragDroppedWrongGestureTarget() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_DROPPED, mockDragboard(DragNDropManager.DATAFORMAT, "abc"), 0, 0, 0, 0, MOVE, null, rootPane, null);
        setDragedViewStatus(mockView("abc", "abc"));
        dragNDropManager.onDragDropped(event);
        assertThat(event.isDropCompleted(), is(false));
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragDroppedDragBoardInvalid() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_DROPPED, mockDragboard(DragNDropManager.DATAFORMAT, "abcd"), 0, 0, 0, 0, MOVE, null, rootPane, null);
        setDragedViewStatus(mockView("abc", "abc"));
        dragNDropManager.onDragDropped(event);
        assertThat(event.isDropCompleted(), is(false));
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragDroppedDragBoardNull() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, MOVE, null, rootPane, null);
        dragNDropManager.onDragDropped(event);
        assertThat(event.isDropCompleted(), is(false));
        assertThat(event.isConsumed(), is(false));
    }
}