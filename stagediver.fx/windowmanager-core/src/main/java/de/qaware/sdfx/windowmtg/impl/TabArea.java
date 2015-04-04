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
package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.*;
import javafx.scene.control.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Describes a logical view area which displays the views within a tab pane.
 *
 * @author christian.fritz
 */
public class TabArea extends ViewArea {

    private static final Logger LOGGER = LoggerFactory.getLogger(TabArea.class);

    private final TabPane tabPane = new TabPane();

    /**
     * A list with all contained views.
     */
    private final Set<ViewStatus> views = new LinkedHashSet<>();

    /**
     * Create a new tab area.
     *
     * @param dragNDropManager Use this drag&drop manager to handle the view management.
     */
    public TabArea(DragNDropManager dragNDropManager) {
        super(dragNDropManager);
        registerDragEvents();

        initFocusEvents();
    }

    /**
     * Create a new tab area.
     *
     * @param parent           Use this area as parent area.
     * @param dragNDropManager Use this drag&drop manager to handle the view management.
     */
    public TabArea(ViewArea parent, DragNDropManager dragNDropManager) {
        super(parent, dragNDropManager);
        registerDragEvents();
        initFocusEvents();
    }

    /**
     * Initialize the focused events.
     * This event handlers handles all the events which would change the current focused view.
     */
    private void initFocusEvents() {
        tabPane.focusedProperty().addListener((observableValue, old, newValue) -> {
            if (newValue && tabPane.getSelectionModel().getSelectedItem() != null) {
                ViewStatus status = (ViewStatus) tabPane.getSelectionModel().getSelectedItem().getUserData();
                getDragNDropManager().getWindowManager().setFocusedView(status.getView());
                LOGGER.debug("Focused tab pane changed, new focused view: {}", status);
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((observableValue, oldTab, newTab) -> {
            if (newTab != null) {
                ViewStatus status = (ViewStatus) newTab.getUserData();
                getDragNDropManager().getWindowManager().setFocusedView(status.getView());
                LOGGER.debug("Tab selection changed, new focused view: {}", newTab.getUserData());
            }
        });
    }

    /**
     * Register the event handler for drag&drop of views.
     */
    private void registerDragEvents() {
        tabPane.setOnDragDetected(event -> getDragNDropManager().onDragDetected(event));
        tabPane.setOnDragDone(event -> getDragNDropManager().onDragDone(event));
        super.registerDragEvents(tabPane);
    }

    /**
     * Remove a view from this area. If this area is empty it will also be removed.
     *
     * @param view The view to remove
     */
    public void remove(ViewStatus view) {
        remove(view, true);
    }

    /**
     * Remove a view from this area. If checkEmpty is true it checks if this area is empty and remove this area.
     *
     * @param view       The view to remove.
     * @param checkEmpty Should this area be removed if it is empty?
     */
    public void remove(ViewStatus view, boolean checkEmpty) {
        if (!views.contains(view)) {
            return;
        }
        LOGGER.info("Remove view {} from areas", view.getView().getViewId());
        views.remove(view);
        view.setArea(null);
        view.setPosition(null);
        tabPane.getTabs().remove(view.getTab());
        if (checkEmpty) {
            handleEmpty();
        }
    }

    /**
     * Check if this area is empty, so remove it.
     *
     * @return True if this area is empty and was successfully removed.
     */
    public boolean handleEmpty() {
        if (views.isEmpty() && (!isEditor() || getRootArea().isCloseStage())) {
            LOGGER.info("Remove empty TabArea {}", this);
            getParent().remove(this);
            return true;
        }
        return false;
    }

    /**
     * Get the javafx scene graph node which represents this area.
     *
     * @return The scene graph node.
     */
    @Override
    public Parent getNode() {
        return tabPane;
    }

    /**
     * Add the view to this area.
     *
     * @param view     The view to add.
     * @param position Add the view at this position.
     */
    @Override
    public void add(ViewStatus view, Position position) {
        if (position != Position.CENTER) {
            super.add(view, position);
            return;
        }
        views.add(view);
        view.setArea(this);
        view.setPosition(position);
        tabPane.getTabs().add(view.getTab());
    }

    /**
     * Is the drop gesture to this area with position center allowed?
     *
     * @return True if a drop to center is allowed.
     */
    @Override
    public boolean dropToCenter() {
        return true;
    }
}
