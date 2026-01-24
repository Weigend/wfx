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
package de.qaware.sdfx.platform.api.events;

import java.util.EventObject;
import java.util.Objects;

/**
 * This event is to report the startup progress to the splash screen and to the global progress bar.
 *
 * @author christian.fritz
 */
public class ProgressEvent extends EventObject {

    /**
     * The event message. It will be shown to the user.
     */
    private final String message;

    /**
     * The progress.
     * The value must be between 0 and 1.
     */
    private final double progress;

    /**
     * ProgressEvent for calculating a progress.
     *
     * @param message  the message the Event will carry
     * @param progress the amount of progress. it must be between 0 and 1.
     * @param source   the object creating this event
     */
    public ProgressEvent(String message, double progress, Object source) {
        super(source);
        this.message = message;
        this.progress = progress;
    }

    /**
     * Returns the Event's message.
     *
     * @return the Event's message or an empty String.
     */
    public String getMessage() {
        return message != null ? message : "";
    }

    /**
     * Returns the Event's progress.
     *
     * @return the Event's progress.
     */
    public double getProgress() {
        return progress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProgressEvent that)) {
            return false;
        }
        return Double.compare(that.progress, progress) == 0
                && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, progress);
    }

    @Override
    public String toString() {
        return "ProgressEvent{message='" + getMessage() + "', progress=" + progress + "}";
    }
}
