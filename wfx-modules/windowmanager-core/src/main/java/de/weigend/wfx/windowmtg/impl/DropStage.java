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
package de.weigend.wfx.windowmtg.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.stage.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The DropStage is a container which handles the drop events of views outside the the application windows.
 * <p/>
 * This drop events are captured by one undecorated and transparent stage per screen. This stages covers the whole screen.
 *
 * @author christian.fritz
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
            Stage stage = initDropStage(screen);
            stages.add(stage);
        }
    }

    /**
     * Init a drop stage for the given screen.
     *
     * @param screen Init a drop stage for this screen.
     * @return The initialized drop stage.
     */
    private Stage initDropStage(Screen screen) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        //stage.initOwner(owner);
        Rectangle2D screenBounds = screen.getVisualBounds();
        //set Stage boundaries to visible bounds of the main screen
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        Pane pane = new Pane();
        pane.setStyle("-fx-background-color: null");
        Scene scene = new Scene(
                pane,
                screenBounds.getWidth(),
                screenBounds.getHeight(),
                Color.color(1, 1, 1, 0.01)
        );

        stage.setScene(scene);
        stage.show();
        initSceneEvents(scene, stage);
        return stage;
    }

    /**
     * Initialize the events for a stage.
     *
     * @param scene The viewed scene.
     * @param stage The stage.
     */
    private void initSceneEvents(final Scene scene, final Stage stage) {
        scene.setOnDragEntered(event -> {
            owner.requestFocus();
            dndManager.getWindowManager().bringToFront();
            event.consume();
        });
        scene.setOnDragOver(event -> {
            Dragboard dragboard = event.getDragboard();
            if (!dragboard.hasContent(DragNDropManager.DATAFORMAT)) {
                event.consume();
                return;
            }
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });
        scene.setOnDragExited(event -> {
            owner.requestFocus();
            dndManager.getWindowManager().bringToFront();
            event.consume();
        });
        scene.setOnDragDropped(event -> dndManager.onDragDroppedNewStage(event, stage));
    }

    /**
     * Close all open stages which are used for the drag&drop gesture.
     */
    public void close() {
        LOGGER.debug("close");
        List<Stage> stagesList = Collections.synchronizedList(stages);
        stagesList.forEach(Stage::close);
        stagesList.clear();
    }
}
