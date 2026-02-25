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

import io.softwareecg.wfx.windowmtg.api.Position;
import io.softwareecg.wfx.windowmtg.api.View;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the current status and additional metadata of an window manager view.
 *
 */
public class ViewStatus {
    public static final int TAB_IMAGE_SIZE = 22;

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewStatus.class);
    /**
     * The registered view
     */
    private final View view;
    private final Position defaultPosition;
    private final ViewStatus parent;
    /**
     * The status whether this view is visible or hidden (or something else)
     */
    private Status status;
    /**
     * The current position within the window
     */
    private Position position;
    private TabArea area;
    /**
     * The tab which contains this view.
     */
    private Tab tab;

    /**
     * Create a new view status.
     *
     * @param view The view to display.
     */
    public ViewStatus(View view) {
        this(view, null);
    }

    /**
     * Create a new view status from a view with the given parent to define the exact position for this view.
     *
     * @param view   The view to display.
     * @param parent The parent view status for exact positioning.
     */
    public ViewStatus(View view, ViewStatus parent) {
        this.view = view;
        this.position = view.getDefaultPosition();
        this.defaultPosition = view.getDefaultPosition();
        this.status = Status.VISIBLE;
        this.parent = parent;
        this.initTab();
    }

    /**
     * Getter for property parent.
     *
     * @return Value for property parent.
     */
    public ViewStatus getParent() {
        return parent;
    }

    /**
     * Getter for property status.
     *
     * @return Value for property status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Setter for property status.
     *
     * @param status Value to set for property status.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Getter for property view.
     *
     * @return Value for property view.
     */
    public View getView() {
        return view;
    }

    /**
     * Getter for property position.
     *
     * @return Value for property position.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Setter for property position.
     *
     * @param position Value to set for property position.
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Getter for property tab.
     *
     * @return Value for property tab.
     */
    public Tab getTab() {
        return tab;
    }

    /**
     * Restore the defaults for this view.
     */
    public void restoreDefault() {
        status = Status.VISIBLE;
        position = defaultPosition;
    }

    /**
     * Initialize the javafx tab.
     */
    private void initTab() {

        tab = new Tab(view.getTitle());
        tab.setClosable(true);
        tab.setContent(view.getRootNode());
        tab.setId(view.getViewId());
        //tab.setTooltip(new Tooltip(view.getToolTipInfo()));
        tab.setUserData(this);
        if (view.getViewImagePath() != null) {
            tab.setGraphic(new ImageView(new Image(
                    view.getViewImagePath().toString(), TAB_IMAGE_SIZE, TAB_IMAGE_SIZE, true, true)));
        }
        tab.setOnClosed(event -> {
            ViewStatus viewStatus = ViewStatus.this;
            viewStatus.getArea().remove(viewStatus);
            viewStatus.setStatus(Status.HIDDEN);
        });
    }

    /**
     * Getter for property area.
     *
     * @return Value for property area.
     */
    public TabArea getArea() {
        return area;
    }

    /**
     * Setter for property area.
     *
     * @param area Value to set for property area.
     */
    public void setArea(TabArea area) {
        this.area = area;
    }

    /**
     * Resize the area of this view to the defined value.
     */
    public void setDividerPositions() {
        SplitPane splitPane;
        final double space = getView().getViewAreaSize();

        if (getArea().getParent().getNode() instanceof SplitPane) {
            splitPane = (SplitPane) getArea().getParent().getNode();
        }
        else {
            return;
        }

        if (space < 0.05 || space > 0.95) {
            return;
        }
        switch (position) {
            case LEFT:      // fall trough
            case TOP:
                splitPane.setDividerPositions(space);
                break;
            case RIGHT:     // fall trough
            case BOTTOM:
                splitPane.setDividerPositions(1 - space);
                break;
            default:
                LOGGER.warn("Invalid position {} given for setting divider positions of {}", position, splitPane);
        }
        LOGGER.debug("Set the divider position to {} for position {}", space, position);
    }

    @Override
    public String toString() {
        return "ViewStatus{" +
                "view=" + view +
                ", parent=" + parent +
                ", status=" + status +
                ", position=" + position +
                '}';
    }

    /**
     * The status of a view.
     */
    protected enum Status {
        /**
         * Indicates that a view is visible as tab within a view area.
         */
        VISIBLE,

        /**
         * Indicates that a view is docked to the side.
         */
        DOCKED,

        /**
         * Indicates that the view is registered but neither docked or viewed.
         */
        HIDDEN,
    }
}
