package de.qaware.sdfx.windowmtg.api;

import javafx.scene.Node;

/**
 * Defines a View. A view is an object that could be registered within the {@link WindowManager} and displayed as a tab.
 */
public interface View {

    /**
     * Get the unique view id for this view.
     *
     * @return The unique view id.
     */
    String getViewId();

    /**
     * Get the title for this view.
     *
     * @return The title
     */
    String getTitle();

    /**
     * Get the tooltip info string.
     *
     * @return The tooltip info.
     */
    String getToolTipInfo();

    /**
     * Get the default position for this view. This position is used to define the initial position where this view is displayed.
     *
     * @return The default position.
     */
    Position getDefaultPosition();

    /**
     * Get the root node for the content area of this view.
     *
     * @return The views content root node.
     */
    Node getRootNode();
}
