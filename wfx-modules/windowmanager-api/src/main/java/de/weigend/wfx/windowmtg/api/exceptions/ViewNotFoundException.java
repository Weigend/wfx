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
package de.weigend.wfx.windowmtg.api.exceptions;

import java.io.IOException;

/**
 * Special exception in case of the fxml view is not found.
 *
 * @author christian.fritz
 */
public class ViewNotFoundException extends IOException {

    /**
     * Default constructor (without message)
     */
    public ViewNotFoundException() {
    }

    /**
     * Only print a message.
     *
     * @param message The message.
     */
    public ViewNotFoundException(String message) {
        super(message);
    }

    /**
     * Print a message and rethrow a other exception as cause.
     *
     * @param message The message.
     * @param cause   The cause.
     */
    public ViewNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Remap a exception as view not found exception.
     *
     * @param cause The previous exception.
     */
    public ViewNotFoundException(Throwable cause) {
        super(cause);
    }
}
