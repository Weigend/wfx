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
package io.softwareecg.wfx.windowmtg.api;

import javafx.geometry.Orientation;

/**
 * The possible positions to place a view within a window.
 * <p>
 * Each non-CENTER position carries the {@link #getSplitOrientation() split
 * orientation} it requires (HORIZONTAL for LEFT/RIGHT, VERTICAL for TOP/BOTTOM)
 * and {@link #isFirstSlot() which side of that split} it occupies (LEFT/TOP =
 * first, RIGHT/BOTTOM = second). This lets the layout logic stay symmetric
 * across all four directions without duplicated switch arms.
 *
 */
public enum Position {

    /**
     * Place the view on the top side.
     */
    TOP(Orientation.VERTICAL, true),

    /**
     * Place the view on the left side.
     */
    LEFT(Orientation.HORIZONTAL, true),

    /**
     * Place the view within the center.
     */
    CENTER(null, false),

    /**
     * Place the window on the right side.
     */
    RIGHT(Orientation.HORIZONTAL, false),

    /**
     * Place the window on the bottom.
     */
    BOTTOM(Orientation.VERTICAL, false);

    private final Orientation splitOrientation;
    private final boolean firstSlot;

    Position(Orientation splitOrientation, boolean firstSlot) {
        this.splitOrientation = splitOrientation;
        this.firstSlot = firstSlot;
    }

    /**
     * The split orientation this position requires when an unsplit area must
     * be split for it. {@code null} for {@link #CENTER}.
     *
     * @return the split orientation, or null for CENTER.
     */
    public Orientation getSplitOrientation() {
        return splitOrientation;
    }

    /**
     * Whether this position occupies the first slot of a split (LEFT or TOP)
     * rather than the second slot (RIGHT or BOTTOM). Undefined for CENTER.
     *
     * @return true if this position is on the first slot of its split.
     */
    public boolean isFirstSlot() {
        return firstSlot;
    }
}
