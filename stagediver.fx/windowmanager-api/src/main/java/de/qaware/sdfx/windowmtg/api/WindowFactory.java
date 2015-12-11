/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
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
package de.qaware.sdfx.windowmtg.api;

import javafx.stage.Stage;

/**
 * A window factory creates the stage of an new managed window. Managed windows are created when an user drops a view
 * outside the WindowManager manged area.
 * <p>
 * This is a {@link FunctionalInterface} whose functional method is {@link #initializeWindow()}.
 *
 * @author christian.fritz
 */
@FunctionalInterface
public interface WindowFactory {
    /**
     * Initialize a new stage that can be used for show managed windows.
     *
     * @return The new stage for a managed window.
     */
    Stage initializeWindow();
}
