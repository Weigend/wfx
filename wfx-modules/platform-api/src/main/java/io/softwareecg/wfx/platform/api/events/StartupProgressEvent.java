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

package io.softwareecg.wfx.platform.api.events;

import java.util.EventObject;

/**
 * Event to publish the startup progress of module loading to the splash screen.
 * <p>
 * This event extends {@link EventObject} directly (not {@link ProgressEvent}) to avoid
 * being delivered to {@link ProgressEvent} subscribers (e.g. the main window's progress bar).
 */
public class StartupProgressEvent extends EventObject {

    private final String message;
    private final double progress;

    /**
     * StartupProgressEvent for reporting module loading progress on the splash screen.
     *
     * @param message  the message the Event will carry
     * @param progress the amount of progress. it must be between 0 and 1.
     * @param source   the object creating this event
     */
    public StartupProgressEvent(String message, double progress, Object source) {
        super(source);
        this.message = message;
        this.progress = progress;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public double getProgress() {
        return progress;
    }
}
