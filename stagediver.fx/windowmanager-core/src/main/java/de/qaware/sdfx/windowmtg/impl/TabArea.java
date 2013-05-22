package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.Position;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.TabPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Describes a logical view area which displays the views within a tabpane.
 */
public class TabArea extends ViewArea {

    private static final Logger LOGGER = LoggerFactory.getLogger(TabArea.class);

    private final TabPane tabPane = new TabPane();

    private final Set<ViewStatus> views = new LinkedHashSet<>();

    public TabArea(DragNDropManager dragNDropManager) {
        super(dragNDropManager);
        registerDragEvents();
    }

    public TabArea(ViewArea parent, DragNDropManager dragNDropManager) {
        super(parent, dragNDropManager);
        registerDragEvents();
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
     * Remove a view from this area. If this area is empty it will also be removed.
     *
     * @param view The view to remove
     */
    public void remove(ViewStatus view) {
        remove(view, true);
    }

    /**
     * Remove a view from this area. If {@param checkEmpty} is true it checks if this area is empty and remove this area.
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
        if (views.isEmpty()) {
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
     * Is the drop gesture to this area with position center allowed?
     *
     * @return True if a drop to center is allowed.
     */
    @Override
    public boolean dropToCenter() {
        return true;
    }
}
