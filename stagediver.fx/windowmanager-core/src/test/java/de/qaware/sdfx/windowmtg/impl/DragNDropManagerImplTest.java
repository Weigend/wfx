package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.JavaFXThreadingRule;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

    @InjectMocks
    private DragNDropManagerImpl dragNDropManager;

    @Before
    public void setUp() throws Exception {
        when(windowManager.getRootPane()).thenReturn(rootPane);
        Whitebox.setInternalState(dragNDropManager, "dropStage", dropStage);
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
}