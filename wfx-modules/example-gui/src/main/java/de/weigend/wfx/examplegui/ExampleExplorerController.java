/*
 * #%L
 * Example GUI Implementation for wfx
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
package de.weigend.wfx.examplegui;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.windowmtg.api.View;
import de.weigend.wfx.windowmtg.api.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This is a example editor controller.
 *
 */
public class ExampleExplorerController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleExplorerController.class);

    @FXML
    protected TreeView<String> tree;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TreeItem<String> root = new TreeItem<>("Root Node");
        root.setExpanded(true);
        root.getChildren().addAll(
                new TreeItem<>("Item 1"),
                new TreeItem<>("Item 2"),
                new TreeItem<>("Item 3")
        );
        tree.setRoot(root);
    }

    /**
     * Event handler for a click on a tree item.
     *
     * @param event the mouse event.
     */
    @FXML
    public void treeClicked(MouseEvent event) {
        LOGGER.info("treeClicked: {}", event);
    }

    /**
     * Focus the editor
     *
     * @param actionEvent the event to focus the editor.
     */
    public void focusEditor(ActionEvent actionEvent) {
        LOGGER.info("focus editor");
        WindowManager windowManager = Lookup.lookup(WindowManager.class);
        View editorView = windowManager.findView("example-1");
        windowManager.showView(editorView);
    }
}
