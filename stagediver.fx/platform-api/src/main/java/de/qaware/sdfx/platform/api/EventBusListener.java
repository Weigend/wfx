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
package de.qaware.sdfx.platform.api;

/**
 * The callback interface.
 *
 * @param <T> the type of the Event
 * @author christian.fritz
 */
public interface EventBusListener<T> {

    /**
     * Method will be called by the bus if the event type is subscribed by this listener.
     *
     * @param event the event.
     * @return true if the event is still valid
     */
    boolean eventPublished(T event);
}
