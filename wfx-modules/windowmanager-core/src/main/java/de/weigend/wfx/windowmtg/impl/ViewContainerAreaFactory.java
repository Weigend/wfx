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
package de.weigend.wfx.windowmtg.impl;

/**
 * Factory to create the {@link de.weigend.wfx.windowmtg.impl.ViewArea} that contains only
 * {@link de.weigend.wfx.windowmtg.api.View}.
 * <p/>
 * For example an implementation will produce {@link de.weigend.wfx.windowmtg.impl.TabArea}. Thats a ViewArea that will
 * show the added views as tabs within a tabpane.
 *
 * @author christian.fritz
 */
public interface ViewContainerAreaFactory {
    /**
     * Create a new view area.
     *
     * @param dragNDropManager The drag&drop manager that handles all drag&drop events.
     * @return The created view Area.
     */
    ViewArea getInstance(DragNDropManager dragNDropManager);

    /**
     * Create a new view area.
     *
     * @param parent           The parent view area for this area.
     * @param dragNDropManager The drag&drop manager that handles all drag&drop events.
     * @return The created view Area.
     */
    ViewArea getInstance(ViewArea parent, DragNDropManager dragNDropManager);
}
