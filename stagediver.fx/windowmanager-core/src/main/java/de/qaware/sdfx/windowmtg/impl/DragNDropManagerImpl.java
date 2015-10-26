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

import de.qaware.sdfx.windowmtg.api.Position;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.input.*;
import javafx.scene.paint.*;
import javafx.stage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Handles the full drag&drop gestures for the window and view management.
 *
 * @author christian.fritz
 */
@Singleton
public class DragNDropManagerImpl implements DragNDropManager {
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DragNDropManagerImpl.class);
    /**
     * Temporal storage for the draged view
     */
    private static ViewStatus dragedViewStatus;
    /**
     * The window manager.
     */
    private final MultiWindowManager windowManager;
    /**
     * The effect for the current drop zone.
     */
    private final Blend effect = new Blend();
    /**
     * The visible effect.
     */
    private final ColorInput dropOverlay = new ColorInput();
    /**
     * Handler for drag&drop outside a window
     */
    private DropStage dropStage;
    /**
     * The current node where the effect is active.
     */
    private Node effectTarget;
    /**
     * Temp stage when the view was dropped outside a stagediver.fx window.
     */
    private Stage droppedStage;

    /**
     * Create a new drag&drop manager instance.
     *
     * @param windowManager The window manager which handles the views and sub windows.
     */
    @Inject
    public DragNDropManagerImpl(MultiWindowManager windowManager) {
        this.windowManager = windowManager;
    }

    /**
     * Getter for property dragedViewStatus.
     *
     * @return Value for property dragedViewStatus.
     */
    public static ViewStatus getDragedViewStatus() {
        return dragedViewStatus;
    }

    /**
     * Set the current view status as draged view.
     *
     * @param dragedViewStatus The view to set as current draged view.
     */
    public static void setDragedViewStatus(ViewStatus dragedViewStatus) {
        DragNDropManagerImpl.dragedViewStatus = dragedViewStatus;
    }

    /**
     * Called to initialize a controller after its root element has been completely processed.
     */
    @Override
    public void init() {
        windowManager.getRootPane().getScene().setOnDragExited(event -> {
            if (dropStage == null) {
                dropStage = new DropStage(DragNDropManagerImpl.this);
                dropStage.show();
            }
            LOGGER.debug("Handle drag exited: {}", event);
            event.consume();
        });
    }

    /**
     * Initialize the drag&drop for a view.
     *
     * @param event The mouse event.
     */
    @Override
    public void onDragDetected(MouseEvent event) {
        if (!(event.getSource() instanceof TabPane)) {
            return;
        }

        boolean withinHeader = false;
        Node target = (Node) event.getTarget();
        while (target != event.getSource()) {
            LOGGER.debug("Target<{}>: {}", target.getClass().getSimpleName(), target);
            if ("TabHeaderArea".equals(target.getClass().getSimpleName())) {
                withinHeader = true;
                break;
            }
            target = target.getParent();
        }
        if (!withinHeader) {
            return;
        }
        LOGGER.debug("Handle drag detected: {}", event);

        TabPane pane = (TabPane) event.getSource();
        ViewStatus view = (ViewStatus) pane.getSelectionModel().getSelectedItem().getUserData();
        setDragedViewStatus(view);

        Dragboard db = pane.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(DATAFORMAT, view.getView().getViewId());

        db.setContent(content);
        if (dropStage == null) {
            dropStage = new DropStage(DragNDropManagerImpl.this);
            dropStage.show();
        }
        event.consume();
    }

    /**
     * Finish the drag&drop gesture.
     *
     * @param event The drag event
     */
    @Override
    public void onDragDone(DragEvent event) {
        if (!(event.getSource() instanceof TabPane) || !(((TabPane) event.getSource()).getUserData() instanceof TabArea)) {
            return;
        }
        LOGGER.debug("Handle drag done: {}", event);
        TabPane source = (TabPane) event.getSource();
        TabArea area = (TabArea) source.getUserData();
        Dragboard db = event.getDragboard();
        if (droppedStage != null) {
            droppedStage.setWidth(droppedStage.getWidth() - 1);
            droppedStage = null;
        }
        if (event.getTransferMode() == TransferMode.MOVE && db.hasContent(DATAFORMAT)) {
            area.handleEmpty();
            closeDropStages();
            getDragedViewStatus().setDividerPositions();
            setDragedViewStatus(null);
        }
        windowManager.redrawAreas();
        event.consume();
    }

    /**
     * Handle Drag&Drop to a invisible stage => opens a new window
     *
     * @param event     The fired event.
     * @param dropStage The stage where the view was dropped.
     */
    @Override
    public void onDragDroppedNewStage(DragEvent event, Stage dropStage) {
        LOGGER.debug("Dropped: {}\n\tSource:\t{}\n\tGestureSource:\t{}\n\tGestureTarget:\t{}",
                event, event.getSource(), event.getGestureSource(), event.getGestureTarget());
        if (isInvalidDragboard(event)) {
            return;
        }

        RootArea area = new RootArea(this, true);
        Stage stage = initManagedWindow(dropStage, area);

        getDragedViewStatus().getArea().remove(getDragedViewStatus(), false);
        getDragedViewStatus().setPosition(Position.CENTER);
        area.add(getDragedViewStatus(), Position.CENTER);
        stage.setTitle(getDragedViewStatus().getView().getTitle());
        stage.show();
        droppedStage = stage;
        windowManager.register(area);
        completeDropped(event, true);
    }

    /**
     * Handle the dropped event for panes. Mainly this event removes the view from the old position and adds it at the
     * new position.
     *
     * @param event The drag event.
     */
    @Override
    public void onDragDropped(DragEvent event) {
        boolean success = false;
        LOGGER.debug("Dropped: {}\n\tSource:\t{}\n\tGestureSource:\t{}\n\tGestureTarget:\t{}",
                event, event.getSource(), event.getGestureSource(), event.getGestureTarget());
        if (isInvalidDragboard(event)) {
            return;
        }
        if (!(event.getGestureTarget() instanceof Control)) {
            return;
        }
        Control targetNode = (Control) event.getGestureTarget();
        // Add view to new area
        if (targetNode.getUserData() instanceof ViewArea) {
            ViewArea target = (ViewArea) targetNode.getUserData();
            getDragedViewStatus().getArea().remove(getDragedViewStatus(), false);
            Position position = detectPosition(event, targetNode);
            getDragedViewStatus().setPosition(position);
            target.add(getDragedViewStatus(), position);
            success = true;
        }
        completeDropped(event, success);
    }

    /**
     * Handle the drag exited event for panes.
     *
     * @param event the drag event.
     */
    @Override
    public void onDragExited(DragEvent event) {
        if (!(event.getSource() instanceof Node)) {
            return;
        }
        Node target = (Node) event.getSource();
        LOGGER.debug("Handle drag exited: {}", event);
        target.setEffect(null);
        event.consume();
    }

    /**
     * Handle the drag over event. It draws the drop position for the current cursor position.
     * <p>
     * Identity check is required here for applying the effect (@SuppressWarnings("PMD.CompareObjectsWithEquals")).
     *
     * @param event The drag event.
     */
    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void onDragOver(DragEvent event) {
        if (!(event.getSource() instanceof Control)) {
            return;
        }
        Control target = (Control) event.getSource();
        if (target != effectTarget) {
            if (effectTarget != null) {
                effectTarget.setEffect(null);
            }
            target.setEffect(effect);
        }
        effect.setMode(BlendMode.COLOR_BURN);
        dropOverlay.setPaint(Color.LIGHTSTEELBLUE);
        effect.setBottomInput(dropOverlay);
        Position position = detectPosition(event, target);

        ViewArea area = (ViewArea) target.getUserData();
        if (!area.dropToCenter() && position == Position.CENTER) {
            event.consume();
            target.setEffect(null);
            effectTarget = null;
            return;
        }
        adjustOverlay(target, position);
        effectTarget = target;
        event.acceptTransferModes(TransferMode.MOVE);
        event.consume();
    }

    /**
     * Get the window manager instance.
     *
     * @return The window manager instance.
     */
    @Override
    public MultiWindowManager getWindowManager() {
        return windowManager;
    }

    /**
     * Complete the dropped event.
     * This contains the cleaning the effects and other status.
     *
     * @param event   The drag event
     * @param success Was the drop gesture successful
     */
    private void completeDropped(DragEvent event, boolean success) {
        LOGGER.debug("Complete dropped event: {}", event);
        if (effectTarget != null) {
            effectTarget.setEffect(null);
        }
        effectTarget = null;

        event.setDropCompleted(success);
        // closeDropStages();
        event.consume();
    }

    /**
     * Validates the dragboard content.
     *
     * @param event The drag drop event.
     * @return False if the dragboard of the event contains a valid view id.
     */
    private boolean isInvalidDragboard(DragEvent event) {
        // Check if dropped content is valid for dropping here
        Dragboard dragboard = event.getDragboard();
        return dragboard == null || !dragboard.hasContent(DATAFORMAT)
                || !dragboard.getContent(DATAFORMAT).equals(getDragedViewStatus().getView().getViewId());
    }

    /**
     * Detect in witch sub area of the rootpane the given dragevent is rised.
     *
     * @param event The drag event
     * @return The position value for the detected sub area.
     */
    private Position detectPosition(DragEvent event, Control source) {
        double areaX = event.getX() / source.getWidth();
        double areaY = event.getY() / source.getHeight();
        if (0.25 <= areaX && areaX < 0.75 && 0.25 <= areaY && areaY < 0.75) {
            return Position.CENTER;
        }
        else if (areaY < 0.25) {
            return Position.TOP;
        }
        else if (areaY >= 0.75) {
            return Position.BOTTOM;
        }
        else if (areaX < 0.25) {
            return Position.LEFT;
        }
        else {
            return Position.RIGHT;
        }
    }

    private void adjustOverlay(Control target, Position position) {
        switch (position) {
            case CENTER:
                dropOverlay.setX(0);
                dropOverlay.setY(0);
                dropOverlay.setWidth(target.getWidth());
                dropOverlay.setHeight(target.getHeight());
                break;
            case LEFT:
                dropOverlay.setX(0);
                dropOverlay.setY(0);
                dropOverlay.setWidth(target.getWidth() * 0.5);
                dropOverlay.setHeight(target.getHeight());
                break;
            case RIGHT:
                dropOverlay.setX(target.getWidth() * 0.5);
                dropOverlay.setY(0);
                dropOverlay.setWidth(target.getWidth() * 0.5);
                dropOverlay.setHeight(target.getHeight());
                break;
            case TOP:
                dropOverlay.setX(0);
                dropOverlay.setY(0);
                dropOverlay.setWidth(target.getWidth());
                dropOverlay.setHeight(target.getHeight() * 0.5);
                break;
            case BOTTOM:
                dropOverlay.setX(0);
                dropOverlay.setY(target.getHeight() * 0.5);
                dropOverlay.setWidth(target.getWidth());
                dropOverlay.setHeight(target.getHeight() * 0.5);
                break;
            default:
        }
    }

    /**
     * Initialize a new managed window.
     *
     * @param dropStage The stage where the view was dropped.
     * @param area      The new root area which should be the new root node for the new stage.
     * @return The new created stage.
     */
    private Stage initManagedWindow(Stage dropStage, final RootArea area) {
        Scene scene = new Scene(area.getNode(), dropStage.getWidth(), dropStage.getHeight());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setWidth(dropStage.getWidth());
        stage.setHeight(dropStage.getHeight());
        stage.setX(dropStage.getX());
        stage.setY(dropStage.getY());
        stage.setOnCloseRequest(event -> windowManager.remove(area));
        return stage;
    }

    /**
     * Close all the invisible drop stages.
     */
    private void closeDropStages() {
        if (dropStage != null) {
            dropStage.close();
            dropStage = null;
        }
    }
}
