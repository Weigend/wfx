/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 Weigend AM
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
package io.softwareecg.wfx.extension.loggerconsole;

import io.softwareecg.wfx.extension.loggerconsole.api.LoggerAdapter;
import io.softwareecg.wfx.lookup.api.Lookup;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.ComboBoxTableCell;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * UI Controller to show the last log messages.
 *
 */
public class ConsoleController implements Initializable {
    @FXML
    private TableView<LoggerAdapter.LoggerLevel> levels;
    @FXML
    private TextArea console;

    @FXML
    private Button clear;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        LoggerAdapter adapter = Lookup.lookup(LoggerAdapter.class);
        adapter.messagesProperty().addListener((o, ov, nv) -> console.setScrollTop(Double.MAX_VALUE));
        console.textProperty().bind(adapter.messagesProperty());
        clear.setOnAction(event -> adapter.clearMessages());

        TableColumn<LoggerAdapter.LoggerLevel, String> levelColumn = (TableColumn<LoggerAdapter.LoggerLevel, String>) levels.getColumns().get(1);
        levelColumn.setCellFactory(ComboBoxTableCell.forTableColumn(adapter.levelsProperty().get()));

        levels.itemsProperty().bindBidirectional(adapter.loggerLevelsProperty());
    }
}
