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
package de.weigend.wfx.platform.api;

import java.util.EventObject;

/**
 * The event bus for loose coupling of separate ui components.
 *
 * @param <T> the type of the Event
 */
public interface EventBus<T extends EventObject> {

    /**
     * Publish a event to all registered listeners. The event ist dispatched by
     * using event.getClass().
     *
     * @param event the event.
     * @return true when the event is still valid.
     */
    boolean publish(T event);

    /**
     * Subscribe for a given type of event. The type is not polymorphic. You
     * have to make a separate subscription for every concrete type.
     *
     * @param type     the event type to subscribe.
     * @param listener your listener, which will be called if a event happens.
     * @param <E>      the concrete event type
     */
    <E extends T> void subscribe(Class<E> type, EventBusListener<? super E> listener);

    /**
     * Unsubscribe a listener for a given type of event.
     *
     * @param type     the event type to unsubscribe from.
     * @param listener the listener to remove.
     * @param <E>      the concrete event type
     * @return true if the listener was successfully removed
     */
    <E extends T> boolean unsubscribe(Class<E> type, EventBusListener<? super E> listener);
}
