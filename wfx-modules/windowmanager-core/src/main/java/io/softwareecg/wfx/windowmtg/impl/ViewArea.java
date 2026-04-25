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
package io.softwareecg.wfx.windowmtg.impl;

import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.windowmtg.api.Position;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ViewArea is a node within the area tree. It has two children which are self view areas.
 *
 */
public class ViewArea {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewArea.class);
    private final SplitPane outerPane;
    private final DragNDropManager dragNDropManager;
    private ViewArea parent;
    private ViewArea firstChild;
    private ViewArea secondChild;
    private Orientation orientation;
    /**
     * Did this area contains the editor pane?
     */
    private boolean editor;

    /**
     * Create a new view area and register the given area as parent.
     *
     * @param parent           The parent area.
     * @param dragNDropManager The drag&drop manager to handle moving the contained views.
     */
    protected ViewArea(ViewArea parent, DragNDropManager dragNDropManager) {
        this(dragNDropManager);
        this.parent = parent;
    }

    /**
     * Create a new view area.
     *
     * @param dragNDropManager The drag&drop manager to handle moving the contained views.
     */
    protected ViewArea(DragNDropManager dragNDropManager) {
        outerPane = new SplitPane();
        outerPane.setOrientation(Orientation.VERTICAL);
        outerPane.getItems().add(new Pane());
        outerPane.getItems().add(new Pane());
        this.dragNDropManager = dragNDropManager;
        registerDragEvents(outerPane);
    }

    /**
     * Register the event handler for drag&drop of views.
     *
     * @param node Register the event handlers on this node.
     */
    protected final void registerDragEvents(Node node) {
        node.setUserData(this);
        node.setOnDragOver(dragNDropManager::onDragOver);
        node.setOnDragExited(dragNDropManager::onDragExited);
        node.setOnDragDropped(dragNDropManager::onDragDropped);
    }

    protected ViewArea getFirstChild() {
        return firstChild;
    }

    /**
     * Set {@param child} as first child of this view area.
     * <p>
     * It will also update the javafx scene graph and the childs parent value.
     *
     * @param child The new child.
     */
    protected void setFirstChild(ViewArea child) {
        this.firstChild = child;
        outerPane.getItems().set(0, child.getNode());
        child.setParent(this);
    }

    /**
     * Get the javafx scene graph node which represents this area.
     *
     * @return The scene graph node.
     */
    public Parent getNode() {
        return outerPane;
    }

    protected ViewArea getSecondChild() {
        return secondChild;
    }

    /**
     * Set {@param child} as second child of this view area.
     * <p>
     * It will also update the javafx scene graph and the childs parent value.
     *
     * @param child The new child.
     */
    protected void setSecondChild(ViewArea child) {
        this.secondChild = child;
        outerPane.getItems().set(1, child.getNode());
        child.setParent(this);
    }

    /**
     * Split this area by {@param orientation}.
     * <p>
     * Either the parameter {@param first} or {@param second} must be this area. Otherwise a
     * {@link IllegalArgumentException} is thrown.
     *
     * @param first       The first element.
     * @param second      The second element.
     * @param orientation The split orientation.
     * @throws IllegalArgumentException In case of both params {@param first} and {@param second} are this or none of
     *                                  them.
     */
    protected void split(ViewArea first, ViewArea second, Orientation orientation) {
        if ((first == this) == (second == this)) {
            throw new IllegalArgumentException("Either first or second area must be this.");
        }

        LOGGER.debug("Split area {} into \n\t{}\n\tand\n\t{}", this, first, second);
        ViewArea area = new ViewArea(parent, dragNDropManager);
        parent.replace(this, area);
        area.setOrientation(orientation);
        area.setFirstChild(first);
        area.setSecondChild(second);
        area.setEditor(area.getFirstChild().isEditor() || area.getSecondChild().isEditor());
    }

    /**
     * Add the view to this area at position.
     * <p>
     * If position is {@link Position#CENTER} it will be added to that child that is defined as editor area.
     * Otherwise this area is split and the view will be positioned according the position parameter.
     *
     * @param view     The view to add.
     * @param position Add the view at this position.
     */
    public void add(ViewStatus view, Position position) {
        LOGGER.debug("Add view {} on {} to area {}", view.getView().getViewId(), position, this);
        if (position == Position.CENTER) {
            ViewArea editorArea = getEditorArea();
            if (editorArea != null) {
                editorArea.add(view, position);
            }
        }
        else {
            // TOP/BOTTOM/LEFT/RIGHT all share the same shape, parameterised
            // by the split orientation and which slot the new view occupies.
            // If this area is already split in the matching orientation,
            // recurse into the matching slot; otherwise create a new split.
            Orientation needed = position.getSplitOrientation();
            boolean firstSlot = position.isFirstSlot();
            if (orientation == needed) {
                (firstSlot ? getFirstChild() : getSecondChild()).add(view, position);
            }
            else {
                ViewContainerAreaFactory viewContainerFactory = Lookup.lookup(ViewContainerAreaFactory.class);
                ViewArea target = viewContainerFactory.getInstance(dragNDropManager);
                target.add(view, Position.CENTER);
                if (firstSlot) {
                    split(target, this, needed);
                }
                else {
                    split(this, target, needed);
                }
            }
        }
        view.setPosition(position);
        view.getArea().getNode().requestLayout();
    }

    /**
     * Remove the given area as child from this area.
     * <p>
     * In case of a underflow this area will also be removed.
     * <p>
     * Identity check is required here (@SuppressWarnings("PMD.CompareObjectsWithEquals")).
     *
     * @param area The area that should be removed.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    protected void remove(ViewArea area) {
        if (area == firstChild) {
            getParent().replace(this, secondChild);
        }
        else if (area == secondChild) {
            getParent().replace(this, firstChild);
        }
    }

    /**
     * Replace the {@param oldArea} with the {@param newArea}.
     * <p>
     * Identity check is required here (@SuppressWarnings("PMD.CompareObjectsWithEquals")).
     *
     * @param oldArea The old area.
     * @param newArea The new area.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    protected void replace(ViewArea oldArea, ViewArea newArea) {
        if (oldArea == firstChild) {
            setFirstChild(newArea);
        }
        else if (oldArea == secondChild) {
            setSecondChild(newArea);
        }
    }

    protected ViewArea getParent() {
        return parent;
    }

    protected void setParent(ViewArea parent) {
        this.parent = parent;
    }

    /**
     * Get that child that is defined as editor
     *
     * @return The editor area.
     */
    private ViewArea getEditorArea() {
        if (getFirstChild().isEditor()) {
            return getFirstChild();
        }
        else if (getSecondChild().isEditor()) {
            return getSecondChild();
        }
        return null;
    }

    /**
     * Is this area or one of its childs the editor area.
     *
     * @return True if this or one of the childs is the editor area.
     */
    public boolean isEditor() {
        return editor;
    }

    /**
     * Define this area as editor area.
     *
     * @param editor Is this area the editor area.
     */
    public void setEditor(boolean editor) {
        this.editor = editor;
    }

    /**
     * Get the the root area where this view is registered.
     *
     * @return The root area of this view.
     */
    public RootArea getRootArea() {
        ViewArea parentArea = this;
        while (parentArea.getParent() != null) {
            parentArea = parentArea.getParent();
        }
        return (RootArea) parentArea;
    }

    protected DragNDropManager getDragNDropManager() {
        return dragNDropManager;
    }

    /**
     * Set the orientation of the split area.
     *
     * @param orientation The orientation of splitting.
     */
    private void setOrientation(Orientation orientation) {
        this.orientation = orientation;
        outerPane.setOrientation(orientation);
    }

    /**
     * Is the drop gesture to this area with position center allowed?
     *
     * @return True if a drop to center is allowed.
     */
    public boolean dropToCenter() {
        return false;
    }

    /**
     * Check if the view area is valid and registered.
     *
     * @return true if the view area is valid. false otherwise.
     */
    public boolean isValid() {
        return getParent() != null && (getParent().getFirstChild() == this || getParent().getSecondChild() == this) && getParent().isValid();
    }
}
