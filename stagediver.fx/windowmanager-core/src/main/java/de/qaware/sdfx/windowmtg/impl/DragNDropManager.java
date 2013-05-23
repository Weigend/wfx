package de.qaware.sdfx.windowmtg.impl;

import javafx.fxml.*;
import javafx.scene.input.*;
import javafx.stage.*;

/**
 * The drag&drop manager.
 */
public interface DragNDropManager extends Initializable {

    /**
     * The specialized data format to handle the drag&drop gestures with managed tabs.
     */
    DataFormat DATAFORMAT = new DataFormat("de.qaware.sdfx.DragNDrop");

    /**
     * Initialize the drag&drop for a view.
     *
     * @param event The mouse event.
     */
    void onDragDetected(MouseEvent event);

    /**
     * Finish the drag&drop gesture.
     *
     * @param event The drag event
     */
    void onDragDone(DragEvent event);

    /**
     * Handle Drag&Drop to a invisible stage => opens a new window
     *
     * @param event     The fired event.
     * @param dropStage The stage where the view was dropped.
     */
    void onDragDroppedNewStage(DragEvent event, Stage dropStage);

    /**
     * Handle the dropped event for panes. Mainly this event removes the view from the old position and adds it at the
     * new position.
     *
     * @param event The drag event.
     */
    void onDragDropped(DragEvent event);

    /**
     * Handle the drag exited event for panes.
     *
     * @param event the drag event.
     */
    void onDragExited(DragEvent event);

    /**
     * Handle the drag over event. It draws the drop position for the current cursor position.
     *
     * @param event The drag event.
     */
    void onDragOver(DragEvent event);

    /**
     * Get the window manager instance.
     *
     * @return The window manager instance.
     */
    MultiWindowManager getWindowManager();
}
