// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 20.06.13 16:15
//     description: Describes a logical view area which displays the views within
//                  a tab pane.
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Describes a logical view area which displays the views within a tab pane.
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
        tabPane.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean old, Boolean newValue) {
                if (newValue) {
                    ViewStatus status = (ViewStatus) tabPane.getSelectionModel().getSelectedItem().getUserData();
                    getDragNDropManager().getWindowManager().setFocusedView(status.getView());
                    LOGGER.debug("Focused tab pane changed, new focused view: {}", status);
                }
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observableValue, Tab oldTab, Tab newTab) {
                if (newTab != null) {
                    ViewStatus status = (ViewStatus) newTab.getUserData();
                    getDragNDropManager().getWindowManager().setFocusedView(status.getView());
                    LOGGER.debug("Tab selection changed, new focused view: {}", newTab.getUserData());
                }
            }
        });
    }

    /**
     * Register the event handler for drag&drop of views.
     */
    private void registerDragEvents() {
        tabPane.setOnDragDetected(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                getDragNDropManager().onDragDetected(event);
            }
        });
        tabPane.setOnDragDone(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                getDragNDropManager().onDragDone(event);
            }
        });
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
