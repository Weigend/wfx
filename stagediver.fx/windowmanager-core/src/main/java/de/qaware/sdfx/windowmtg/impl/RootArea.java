package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.Position;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.*;

/**
 *
 */
public final class RootArea extends ViewArea {
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
        ViewArea editorArea = new TabArea(this, dragNDropManager);
        editorArea.setEditor(true);
        this.box.getChildren().add(editorArea.getNode());
        setFirstChild(editorArea);
    }

    @Override
    public void add(ViewStatus view, Position position) {
        getFirstChild().add(view, position);
    }

    @Override
    public Parent getNode() {
        return box;
    }

    @Override
    protected void setFirstChild(ViewArea child) {
        super.setFirstChild(child);
        box.getChildren().set(0, child.getNode());
        HBox.setHgrow(child.getNode(), Priority.ALWAYS);
        VBox.setVgrow(child.getNode(), Priority.ALWAYS);
    }

    @Override
    protected void split(ViewArea first, ViewArea second, Orientation orientation) {
        throw new UnsupportedOperationException("Root Areas can not be split");
    }

    @Override
    protected void remove(ViewArea area) {
        if (!closeStage) {
            throw new UnsupportedOperationException("Root Areas must have exactly one child");
        }
        ((Stage) box.getScene().getWindow()).close();
    }

    @Override
    protected void setSecondChild(ViewArea child) {
        throw new UnsupportedOperationException("Root Areas can not contain more than one ");
    }

    @Override
    protected void setParent(ViewArea parent) {
        throw new UnsupportedOperationException("Root Areas can not have any parent area");
    }
}
