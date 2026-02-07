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
package de.weigend.wfx.extension.uiutils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.List;

/**
 * The {@code ToolMenuBarUtils} contains helpful methods to programmatically change toolbars and menu bars.
 *
 * @author christian.fritz
 */
public final class ToolBarUtils {

    public static final int TOOLBAR_IMAGE_SIZE = 37;

    private ToolBarUtils() {
    }

    /**
     * Add an new button to a target list after a specific {@code searchId}. The {@code searchId} is compared against
     * the {@link Node#getId()} method. If the searched id is not found the button will be added to the end of the
     * list.
     *
     * @param targetList     Add the button to this list.
     * @param searchId       The id of the element to search for.
     * @param targetId       The id of the new button.
     * @param tooltip        The tooltip of the button.
     * @param image          The url for the shown image on the button.
     * @param onClickHandler The event handler executed on a click on the button.
     * @return The created and added button.
     */
    public static Button addButtonAfter(List<Node> targetList, String searchId, String targetId,
                                        String tooltip, URL image, EventHandler<ActionEvent> onClickHandler) {
        int index = findIndex(targetList, searchId);
        Button button = new Button();
        button.setId(targetId);
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(onClickHandler);
        ImageView value = new ImageView(new Image(image.toExternalForm()));
        value.setFitWidth(TOOLBAR_IMAGE_SIZE);
        value.setFitHeight(TOOLBAR_IMAGE_SIZE);
        button.setGraphic(value);
        targetList.add(index + 1, button);
        return button;
    }

    /**
     * Find the given id on the first level of the given list of nodes.
     *
     * @param nodes Search in this list.
     * @param id    The id to search for.
     * @return The found index or the last index of the list if the id was not found.
     */
    public static int findIndex(List<Node> nodes, String id) {
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (StringUtils.equals(node.getId(), id)) {
                return i;
            }
        }
        return nodes.size() - 1;
    }
}
