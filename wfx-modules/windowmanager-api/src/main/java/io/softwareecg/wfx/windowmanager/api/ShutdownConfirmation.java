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

import javafx.stage.Stage;

/**
 * Strategy for confirming application shutdown when the user closes the
 * main window.
 * <p>
 * WFX ships a default implementation that displays a Yes/No dialog from
 * an FXML resource. Applications that want a different look — branded
 * artwork, additional context, "save unsaved data?" prompts — register
 * their own {@code @Singleton} implementation; with the WFX default
 * marked {@code @Secondary}, Avaje DI selects the application-provided
 * bean automatically. No subclassing of
 * {@link io.softwareecg.wfx.windowmanager.windows.DefaultApplicationWindow
 * DefaultApplicationWindow} required.
 *
 * @since 1.1.0
 */
public interface ShutdownConfirmation {

    /**
     * Ask the user (or the application logic) whether shutdown should
     * proceed.
     * <p>
     * Implementations may show a modal dialog, check for unsaved changes,
     * delegate to a chain of veto subscribers, or any combination.
     *
     * @param owner the main application stage — use as parent for any
     *              modal dialog so it inherits modality and focus
     *              correctly. Never {@code null}.
     * @return {@code true} to proceed with shutdown, {@code false} to
     *         cancel and keep the application running.
     */
    boolean confirm(Stage owner);
}
