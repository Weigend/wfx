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
package de.weigend.wfx.windowmtg.api;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Defines the main window of the wfx platform.
 *
 */
public interface ApplicationWindow {

    /**
     * Initialize the application window.
     *
     * @throws IOException In case of any error when loading the fxml files.
     */
    void init() throws IOException;

    /**
     * Get all menus within the menu bar.
     *
     * @return A list with all menus.
     */
    ObservableList<Menu> getMenu();

    /**
     * Get all items which are placed within the tool bar.
     *
     * @return A list with all nodes.
     */
    ObservableList<Node> getToolbarItems();

    /**
     * Get the items which are currently placed in the status bar.
     *
     * @return A list with all items in the status bar.
     */
    ObservableList<Node> getStatusBarItems();

    /**
     * Get the window manager for the plattform.
     *
     * @return The window manager.
     */
    WindowManager getWindowManager();

    /**
     * Add the window manager to the main window.
     *
     * @param windowManager The window manager.
     */
    void setWindowManager(WindowManager windowManager);

    /**
     * Restore the default title.
     */
    void restoreTitle();

    /**
     * Get the current title of this window.
     *
     * @return The title.
     */
    String getTitle();

    /**
     * Set the new title for this window.
     *
     * @param title The new title.
     */
    void setTitle(String title);

    /**
     * Get the the javafx stage for this window.
     *
     * @return The stage where this window is shown.
     */
    Stage getStage();

    /**
     * Set the javafx stage for this window.
     *
     * @param stage The stage for this window.
     */
    void setStage(Stage stage);
}
