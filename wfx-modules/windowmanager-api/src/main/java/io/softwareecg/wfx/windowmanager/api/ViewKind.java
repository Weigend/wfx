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
 * Classifies a {@link View} by lifecycle role.
 * <p>
 * The kind drives three coupled behaviours in the window manager: how
 * {@code closeView} disposes of the view, whether {@code restoreDefaultLayout}
 * brings it back, and whether the platform-built View menu offers an entry
 * for it. See the table on the {@link WindowManager} for the full matrix.
 *
 * @since 1.1.0
 */
public enum ViewKind {

    /**
     * A persistent application panel (Explorer, Outline, Logger Console …).
     * <ul>
     *     <li>Closing the tab hides the view; it stays in the registry and
     *     can be reopened from the View menu.</li>
     *     <li>{@code restoreDefaultLayout} re-shows it in its original
     *     default position.</li>
     *     <li>The platform's auto-built View menu lists one entry per
     *     {@code TOOL} view in registration order.</li>
     * </ul>
     */
    TOOL,

    /**
     * A transient, content-bound view (a chart for one JAR, an editor for
     * one file, a query result …).
     * <ul>
     *     <li>Closing the tab unregisters it permanently — the surrounding
     *     module is responsible for opening a fresh one when needed.</li>
     *     <li>{@code restoreDefaultLayout} drops every {@code DOCUMENT}.</li>
     *     <li>{@code DOCUMENT} views never appear in the auto-built View
     *     menu.</li>
     * </ul>
     */
    DOCUMENT
}
