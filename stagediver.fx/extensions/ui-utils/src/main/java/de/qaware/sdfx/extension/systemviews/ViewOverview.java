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
package de.qaware.sdfx.extension.systemviews;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.View;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * View controller to list all registered views.
 *
 * @author christian.fritz
 */
public class ViewOverview implements Initializable {
    private static final double IMAGE_SIZE = 16;
    @FXML
    private ListView<View> views;

    private WindowManager windowManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        windowManager = Lookup.lookup(WindowManager.class);
        SortedList<View> items = new SortedList<>(windowManager.getRegisteredViews());
        items.setComparator((o1, o2) -> o1.getTitle().compareTo(o2.getTitle()));
        views.setItems(items);

        views.setCellFactory(param -> new ListCell<View>() {
            @Override
            protected void updateItem(View item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    return;
                }
                setId("overview_" + item.getViewId());
                setText(item.getTitle());
                if (item.getViewImagePath() != null) {
                    setGraphic(new ImageView(new Image(
                            item.getViewImagePath().toString(), IMAGE_SIZE, IMAGE_SIZE, true, true)));
                }
            }

        });
        views.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                showView();
            }
        });
        views.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                showView();
            }
        });
    }

    private void showView() {
        windowManager.showView(views.getSelectionModel().getSelectedItem());
    }
}
