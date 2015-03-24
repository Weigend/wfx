// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 23.05.13 10:53
//     description: Defines the root view area.
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.Position;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.*;

/**
 * A RootArea is a special {@link ViewArea} which has no parent and is directly used as root.
 */
public class RootArea extends ViewArea {
    private final Pane box;
    /**
     * Close the stage containing this area when removing the child.
     */
    private final boolean closeStage;


    /**
     * Create a new root area.
     *
     * @param dragNDropManager The drag&drop manager
     * @param closeStage       Close the stage containing this area when the last view was removed?
     */
    public RootArea(DragNDropManager dragNDropManager, boolean closeStage) {
        this(new VBox(), dragNDropManager, closeStage);
    }

    /**
     * Create a new root area.
     *
     * @param box              Use this pane to draw all the content.
     * @param dragNDropManager The drag&drop manager
     * @param closeStage       Close the stage containing this area when the last view was removed?
     */
    public RootArea(Pane box, DragNDropManager dragNDropManager, boolean closeStage) {
        super(dragNDropManager);
        this.closeStage = closeStage;
        this.box = box;
        ViewArea editorArea = Lookup.lookup(ViewContainerAreaFactory.class).getInstance(this, dragNDropManager);
        editorArea.setEditor(true);
        this.box.getChildren().add(editorArea.getNode());
        setFirstChild(editorArea);
        HBox.setHgrow(box, Priority.ALWAYS);
        VBox.setVgrow(box, Priority.ALWAYS);
    }

    /**
     * Set {@param child} as first child of this view area.
     * <p>
     * It will also update the javafx scene graph and the childs parent value.
     *
     * @param child The new child.
     */
    @Override
    protected void setFirstChild(ViewArea child) {
        super.setFirstChild(child);
        box.getChildren().set(0, child.getNode());
        HBox.setHgrow(child.getNode(), Priority.ALWAYS);
        VBox.setVgrow(child.getNode(), Priority.ALWAYS);
    }

    @Override
    public Parent getNode() {
        return box;
    }

    @Override
    protected void setSecondChild(ViewArea child) {
        throw new UnsupportedOperationException("Root Areas can not contain more than one ");
    }

    @Override
    protected void split(ViewArea first, ViewArea second, Orientation orientation) {
        throw new UnsupportedOperationException("Root Areas can not be split");
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
    @Override
    public void add(ViewStatus view, Position position) {
        getFirstChild().add(view, position);
    }

    @Override
    protected void remove(ViewArea area) {
        if (!closeStage) {
            throw new UnsupportedOperationException("Root Areas must have exactly one child");
        }
        ((Stage) box.getScene().getWindow()).close();
    }

    @Override
    protected void setParent(ViewArea parent) {
        throw new UnsupportedOperationException("Root Areas can not have any parent area");
    }

    /**
     * Whether or not the stage will be closed when removing the child.
     *
     * @return true, close the stage when removing the child.
     */
    public boolean isCloseStage() {
        return closeStage;
    }
}
