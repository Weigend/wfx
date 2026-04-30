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
package io.softwareecg.wfx.windowmtg.windows;

import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.windowmtg.api.ApplicationWindow;
import io.softwareecg.wfx.windowmtg.api.ShutdownConfirmation;
import io.softwareecg.wfx.windowmtg.api.WindowManager;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This is the main window of the wfx platform. It supports the window management and the default bars like
 * menu, tool and status bar.
 *
 */
@Singleton
public class DefaultApplicationWindow implements ApplicationWindow {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultApplicationWindow.class);
    @FXML
    private MenuBar menuBar;
    @FXML
    private ToolBar toolbar;
    @FXML
    private Pane statusBar;
    private WindowManager windowManager;
    private Stage stage;
    private String title;
    private String defaultTitle;

    /**
     * Initialize the application window.
     *
     * @throws IOException In case of the requested fxml file can not be found or read.
     */
    public void init() throws IOException {
        FXMLLoader loader = Lookup.lookup(FXMLLoader.class);
        loader.setLocation(getClass().getResource("/io/softwareecg/wfx/windowmtg/windows/DefaultApplicationWindow.fxml"));
        loader.setController(this);
        BorderPane rootPane = loader.load();
        stage.setScene(new Scene(rootPane));
        rootPane.setCenter(windowManager.getRootPane());

        useSystemMenuBarIfPossible();
    }

    /**
     * Get a list with all menu items.
     *
     * @return A list with the menu items.
     */
    @Override
    public ObservableList<Menu> getMenu() {
        return menuBar.getMenus();
    }

    /**
     * Get a list with all tool bar items.
     *
     * @return A list with the toolbar items.
     */
    @Override
    public ObservableList<Node> getToolbarItems() {
        return toolbar.getItems();
    }

    @Override
    public ObservableList<Node> getStatusBarItems() {
        return statusBar.getChildren();
    }

    @Override
    public WindowManager getWindowManager() {
        return windowManager;
    }

    @Override
    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    @Override
    public void restoreTitle() {
        stage.setTitle(defaultTitle);
    }

    @Override
    public String getTitle() {
        return stage.getTitle();
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        if (getStage() != null) {
            getStage().setTitle(title);
        }
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        this.defaultTitle = stage.getTitle();
        this.stage.setTitle(title);
        this.stage.setOnCloseRequest(this::platformShutdownRequestHandler);
    }

    /**
     * Event handler that will be executed when request to close the main stage.
     * <p>
     * Delegates the user-facing decision to a {@link ShutdownConfirmation}
     * looked up via {@link Lookup}. Applications override the prompt by
     * registering their own {@code @Singleton ShutdownConfirmation} bean —
     * Avaje DI selects it over the {@code @Secondary} default. Subclasses
     * that need to take over the close handler completely can still
     * override this method.
     *
     * @param event The window event triggered the handler.
     */
    protected void platformShutdownRequestHandler(WindowEvent event) {
        if (Lookup.lookup(ShutdownConfirmation.class).confirm(stage)) {
            Platform.exit();
            // Force JVM exit in case non-daemon threads are still running.
            System.exit(0);
        }
        else {
            event.consume();
        }
    }

    /**
     * Uses the system menu bar on Mac OSX systems.
     */
    protected final void useSystemMenuBarIfPossible() {
        String os = System.getProperty("os.name");
        if (menuBar != null && StringUtils.startsWith(os, "Mac")) {
            menuBar.useSystemMenuBarProperty().set(true);
        }
    }
}
