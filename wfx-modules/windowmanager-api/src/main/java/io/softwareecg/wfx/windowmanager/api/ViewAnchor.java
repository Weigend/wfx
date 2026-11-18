/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2026 Weigend AM
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
package io.softwareecg.wfx.windowmanager.api;

/**
 * What the {@linkplain View#getDefaultPosition() default position} of a
 * {@link View} is measured from.
 * <p>
 * A panel at {@link Position#BOTTOM} can mean two things: a strip along the
 * bottom of the whole window, under everything else, or a strip under the
 * documents only, between the panels to their left and right. The position
 * says which side; the anchor says of what.
 * <p>
 * The anchor applies when the view is placed by its defaults: by
 * {@link WindowManager#register(View)}, by
 * {@link WindowManager#restoreDefaultLayout()}, and when
 * {@link WindowManager#showView(View)} brings back a hidden view. A view
 * registered with a parent view is placed next to that parent instead.
 *
 * @since 1.4.0
 */
public enum ViewAnchor {

    /**
     * The side of the main window's content: the view takes its share from
     * everything that is there when it is placed. Where it ends up therefore
     * depends on what was registered before it.
     */
    WINDOW,

    /**
     * The side of the editor pane — the pane of the main window that takes
     * the views at {@link Position#CENTER}. The view takes its share from
     * that pane alone and leaves the panels around it as they are, no matter
     * in which order the views are registered.
     * <p>
     * The editor pane is there from the start and stays when its last view
     * is closed, so a view can be placed beside it before any document is
     * open. This is how a panel that belongs to the documents — a console,
     * a problems list, the details of the selection — keeps its place under
     * them: it needs no document to be docked to and can be registered at
     * startup like any other tool.
     * <p>
     * For a view at {@link Position#CENTER} the anchor makes no difference:
     * it goes into the editor pane either way.
     */
    EDITOR
}
