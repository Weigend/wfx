/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
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

import de.weigend.wfx.windowmtg.api.WindowManager;

import javafx.beans.property.*;
import javafx.collections.*;

/**
 * A multi window manager.
 * <p/>
 * This is a window manager which is able to handle the views within one or more windows.
 *
 * @author Christian Fritz
 */
public interface MultiWindowManager extends WindowManager {

    /**
     * Register a new root area as subwindow.
     *
     * @param area The new root area.
     */
    void register(RootArea area);

    /**
     * Bring all windows managed by this window manager to front.
     */
    void bringToFront();

    /**
     * Remove and close the given root area.
     *
     * @param area The root area to remove.
     */
    void remove(RootArea area);

    /**
     * Get the root area of the main window.
     *
     * @return The root area of the main window
     */
    RootArea getMainRootArea();

    /**
     * Get the property to observe the main root area.
     *
     * @return The main root area property.
     */
    ReadOnlyObjectProperty<RootArea> mainRootAreaProperty();

    /**
     * Get the observable list of root areas.
     *
     * @return The root areas list.
     */
    ObservableList<RootArea> getRootAreas();

    /**
     * Request the redrawing of all areas.
     */
    void redrawAreas();
}
