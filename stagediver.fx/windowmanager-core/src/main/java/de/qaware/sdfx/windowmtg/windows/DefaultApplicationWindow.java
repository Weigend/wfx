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
package de.qaware.sdfx.windowmtg.windows;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
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

import javax.inject.Singleton;
import java.io.IOException;
import java.util.Objects;

/**
 * This is the main window of the stagediver.fx platform. It supports the window management and the default bars like
 * menu, tool and status bar.
 *
 * @author christian.fritz
 */
@Singleton
@javax.annotation.Priority(Integer.MIN_VALUE)
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
    private String defaultTitle;

    /**
     * Initialize the application window.
     *
     * @throws IOException In case of the requested fxml file can not be found or read.
     */
    public void init() throws IOException {
        FXMLLoader loader = Lookup.lookup(FXMLLoader.class);
        loader.setLocation(getClass().getResource("/de/qaware/sdfx/windowmtg/windows/DefaultApplicationWindow.fxml"));
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
        stage.setTitle(title);
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        this.defaultTitle = stage.getTitle();

        this.stage.setOnCloseRequest(this::platformShutdownRequestHandler);
    }

    /**
     * Event handler that will be executed when request to close the main stage.
     * <p>
     * It can be overwritten to perform a own action.
     *
     * @param event The window event triggered the handler.
     */
    protected void platformShutdownRequestHandler(WindowEvent event) {
        Dialog<Boolean> d = new Dialog<>();
        d.initOwner(stage);
        d.setResultConverter(b -> Objects.equals(b, ButtonType.YES));
        FXMLLoader loader = Lookup.lookup(FXMLLoader.class);
        loader.setLocation(getClass().getResource("/de/qaware/sdfx/windowmtg/windows/ShutdownDialog.fxml"));
        try {
            d.setDialogPane(loader.load());
            d.showAndWait().ifPresent(shouldClose -> {
                if (!shouldClose) {
                    event.consume();
                }
                else {
                    Platform.exit();
                }
            });
        }
        catch (IOException e) {
            LOGGER.error("Unable to load shutdown dialog.", e);
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
