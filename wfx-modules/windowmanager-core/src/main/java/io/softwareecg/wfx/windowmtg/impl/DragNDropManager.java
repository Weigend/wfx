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

import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * The drag&drop manager. The implementations handles the full dnd management of views.
 *
 */
public interface DragNDropManager {

    /**
     * The specialized data format to handle the drag&drop gestures with managed tabs.
     */
    DataFormat DATAFORMAT = new DataFormat("io.softwareecg.wfx.DragNDrop");

    /**
     * Called to initialize a controller after its root element has been completely processed.
     */
    void init();

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
