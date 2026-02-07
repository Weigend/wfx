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
package de.weigend.wfx.windowmtg.api;

import javafx.scene.Parent;

import java.net.URL;

/**
 * Defines a View. A view is an object that could be registered within the {@link WindowManager} and displayed as a
 * tab.
 *
 * @author christian.fritz
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
     * Get the default position for this view. This position is used to define the initial position where this view is
     * displayed.
     *
     * @return The default position.
     */
    Position getDefaultPosition();

    /**
     * Get the root node for the content area of this view.
     *
     * @return The views content root node.
     */
    Parent getRootNode();

    /**
     * Get the path for an image shown within the tab and view overview.
     * <p>
     * Return null if the view should not have an image.
     *
     * @return The url to find the image.
     */
    default URL getViewImagePath() {return null;}

    /**
     * Get the view area size. This will be a number between 0 and 1 which defines the percentage space of this view
     * within the surrounding area.
     *
     * @return The view area size.
     */
    double getViewAreaSize();
}
