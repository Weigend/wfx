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

package de.weigend.wfx.platform.api.events;

/**
 * Event to publish the startup progress of an defined module.
 *
 * @author christian.fritz
 */
public class StartupProgressEvent extends ProgressEvent {
    /**
     * ProgressEvent for calculating a progress.
     *
     * @param message  the message the Event will carry
     * @param progress the amount of progress. it must be between 0 and 1.
     * @param source   the object creating this event
     */
    public StartupProgressEvent(String message, double progress, Object source) {
        super(message, progress, source);
    }
}
