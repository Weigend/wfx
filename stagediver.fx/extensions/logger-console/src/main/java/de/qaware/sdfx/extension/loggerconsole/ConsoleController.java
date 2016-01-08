/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 QAware GmbH
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
package de.qaware.sdfx.extension.loggerconsole;

import de.qaware.sdfx.extension.loggerconsole.api.LoggerAdapter;
import de.qaware.sdfx.lookup.Lookup;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * UI Controller to show the last log messages.
 *
 * @author christian.fritz
 */
public class ConsoleController implements Initializable {
    @FXML
    private TextArea console;
    @FXML
    private ChoiceBox<String> level;
    @FXML
    private Button clear;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LoggerAdapter adapter = Lookup.lookup(LoggerAdapter.class);
        adapter.levelProperty().bind(level.getSelectionModel().selectedItemProperty());
        adapter.messagesProperty().addListener((o, ov, nv) -> console.setScrollTop(1));
        console.textProperty().bind(adapter.messagesProperty());
        level.setItems(adapter.levelsProperty());
        clear.setOnAction(event -> adapter.clearMessages());
    }
}
