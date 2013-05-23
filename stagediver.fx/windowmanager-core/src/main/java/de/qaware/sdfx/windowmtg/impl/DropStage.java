package de.qaware.sdfx.windowmtg.impl;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The DropStage is a container which handles the drop events of views outside the the application windows.
 * <p/>
 * This drop events are captured by one undecorated and transparent stage per screen. This stages covers the whole screen.
 */
class DropStage {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropStage.class);

    /**
     * The drag&drop manager
     */
    private final DragNDropManager dndManager;

    /**
     * The the primary stage containing the window manager.
     */
    private final Stage owner;

    /**
     * A list with all stages (one per screen) which are used as drop areas.
     */
    private final List<Stage> stages = new ArrayList<>();

    /**
     * Initialize the drop stages for a new drag&drop gesture.
     *
     * @param dndManager The drag&drop manager which handles the events.
     */
    public DropStage(final DragNDropManager dndManager) {
        this.dndManager = dndManager;
        this.owner = (Stage) dndManager.getWindowManager()
                .getRootPane()
                .getScene()
                .getWindow();
    }

    /**
     * Show the drop stages.
     */
    public void show() {
        List<Screen> screenList = Screen.getScreens();

        for (Screen screen : screenList) {
            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);
            //stage.initOwner(owner);
            Rectangle2D screenBounds = screen.getVisualBounds();
            //set Stage boundaries to visible bounds of the main screen
            stage.setX(screenBounds.getMinX());
            stage.setY(screenBounds.getMinY());
            stage.setWidth(screenBounds.getWidth());
            stage.setHeight(screenBounds.getHeight());

            Scene scene = new Scene(
                    new Pane(),
                    screenBounds.getWidth(),
                    screenBounds.getHeight(),
                    Color.color(1, 1, 1, 0.01)
            );

            stage.setScene(scene);
            stage.show();
            initSceneEvents(scene, stage);
            stages.add(stage);
        }
    }

    /**
     * Initialize the events for a stage.
     *
     * @param scene The viewed scene.
     * @param stage The stage.
     */
    private void initSceneEvents(final Scene scene, final Stage stage) {
        scene.setOnDragEntered(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                owner.requestFocus();
                dndManager.getWindowManager().bringToFront();
                event.consume();
            }
        });
        scene.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard dragboard = event.getDragboard();
                if (!dragboard.hasContent(DragNDropManager.DATAFORMAT)) {
                    event.consume();
                    return;
                }
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });
        scene.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                owner.requestFocus();
                dndManager.getWindowManager().bringToFront();
                event.consume();
            }
        });
        scene.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                dndManager.onDragDroppedNewStage(event, stage);
            }
        });
    }

    /**
     * Close all open stages which are used for the drag&drop gesture.
     */
    public void close() {
        LOGGER.debug("close");
        for (Stage stage : new ImmutableList.Builder<Stage>().addAll(stages).build()) {
            stage.close();
            stages.remove(stage);
        }
    }
}
