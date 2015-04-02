package de.qaware.sdfx.windowmtg.impl;

import com.sun.javafx.tk.Toolkit;
import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.windowmtg.api.JavaFXThreadingRule;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.input.*;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static de.qaware.sdfx.windowmtg.impl.DragNDropManagerImpl.setDragedViewStatus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
        ViewContainerAreaFactory containerAreaFactory = mock(ViewContainerAreaFactory.class);
        when(lookupStrategy.lookup(ViewContainerAreaFactory.class)).thenReturn(containerAreaFactory);
    }

    @Test
    public void testInit() throws Exception {
        dragNDropManager.init();
        DragEvent event = new DragEvent(null, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, TransferMode.MOVE, null, rootPane, null);
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
    public void testOnDragDoneWrongSource() throws Exception {
        DragEvent event = new DragEvent(new Pane(), scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, TransferMode.MOVE, null, rootPane, null);
        dragNDropManager.onDragDone(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragDoneWrongUserData() throws Exception {
        TabPane source = new TabPane();
        source.setUserData(new Object());
        DragEvent event = new DragEvent(source, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, TransferMode.MOVE, null, rootPane, null);
        dragNDropManager.onDragDone(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragExited() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, TransferMode.MOVE, null, rootPane, null);
        dragNDropManager.onDragExited(event);
        assertThat(node.getEffect(), is(nullValue()));
        assertThat(event.isConsumed(), is(true));
    }

    @Test
    public void testOnDragExitedWrongSource() throws Exception {
        DragEvent event = new DragEvent(new Object(), scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, TransferMode.MOVE, null, rootPane, null);
        dragNDropManager.onDragExited(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragOverWrongSource() throws Exception {
        DragEvent event = new DragEvent(new Object(), scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, TransferMode.MOVE, null, rootPane, null);
        dragNDropManager.onDragOver(event);
        assertThat(event.isConsumed(), is(false));
    }

    @Test
    public void testOnDragDroppedNewStageDragBoardNull() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, TransferMode.MOVE, null, rootPane, null);
        Stage stage = mock(Stage.class);
        dragNDropManager.onDragDroppedNewStage(event, stage);
        assertThat(event.isDropCompleted(), is(false));
    }

    @Test
    public void testOnDragDroppedNewStageDragBoardInvalid() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_EXITED, mockDragboard(DataFormat.PLAIN_TEXT, "abc"), 0, 0, 0, 0, TransferMode.MOVE, null, rootPane, null);
        Stage stage = mock(Stage.class);
        dragNDropManager.onDragDroppedNewStage(event, stage);
        assertThat(event.isDropCompleted(), is(false));
    }

    @Test
    public void testOnDragDroppedWrongGestureTarget() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, TransferMode.MOVE, null, rootPane, null);
        dragNDropManager.onDragDropped(event);
        assertThat(event.isDropCompleted(), is(false));
    }

    @Test
    public void testOnDragDroppedDragBoardNull() throws Exception {
        Node node = mock(Node.class);
        DragEvent event = new DragEvent(node, scene, DragEvent.DRAG_EXITED, null, 0, 0, 0, 0, TransferMode.MOVE, null, rootPane, null);
        dragNDropManager.onDragDropped(event);
        assertThat(event.isDropCompleted(), is(false));
    }

    private Dragboard mockDragboard(DataFormat dateFormat, String viewId) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<Dragboard> constructor = Dragboard.class.getDeclaredConstructor(com.sun.javafx.tk.TKClipboard.class);
        constructor.setAccessible(true);
        Dragboard dragboard = constructor.newInstance(Toolkit.getToolkit().createLocalClipboard());

        ClipboardContent content = new ClipboardContent();
        content.put(dateFormat, viewId);

        dragboard.setContent(content);
        return dragboard;
    }
}