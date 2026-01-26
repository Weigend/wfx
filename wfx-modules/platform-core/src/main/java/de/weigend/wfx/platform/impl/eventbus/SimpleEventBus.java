/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
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
package de.weigend.wfx.platform.impl.eventbus;

import de.weigend.wfx.platform.api.EventBus;
import de.weigend.wfx.platform.api.EventBusListener;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A thread-safe event bus with support for event type hierarchies.
 * <p>
 * Features:
 * <ul>
 *   <li>Thread-safe subscription management using {@link ConcurrentHashMap}</li>
 *   <li>Safe iteration during event publishing using {@link CopyOnWriteArrayList}</li>
 *   <li>Event hierarchy support - listeners for superclasses receive events of subclasses</li>
 *   <li>Error isolation - exceptions in one listener don't affect others</li>
 * </ul>
 *
 * @author christian.fritz
 */
@Singleton
@SuppressWarnings({"rawtypes", "unchecked"})
public class SimpleEventBus implements EventBus {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleEventBus.class);

    private final Map<Class, List<EventBusListener>> subscriptions = new ConcurrentHashMap<>();

    @Override
    public boolean publish(EventObject event) {
        Set<EventBusListener> listeners = collectListeners(event.getClass());
        if (listeners.isEmpty()) {
            LOG.debug("No listeners registered for event type: {}", event.getClass().getName());
            return true;
        }

        boolean result = true;
        for (EventBusListener listener : listeners) {
            try {
                result &= listener.eventPublished(event);
            } catch (Exception e) {
                LOG.error("Exception in event listener for {}: {}", event.getClass().getSimpleName(), e.getMessage(), e);
                // Continue with other listeners - don't let one broken listener stop the bus
            }
        }
        return result;
    }

    /**
     * Collects all unique listeners that should receive the event, including listeners
     * registered for superclasses of the event type. Each listener is only included once,
     * even if registered for multiple types in the hierarchy.
     *
     * @param eventType the concrete event type
     * @return set of all applicable listeners (deduplicated)
     */
    private Set<EventBusListener> collectListeners(Class<?> eventType) {
        Set<EventBusListener> result = new HashSet<>();

        // Walk up the class hierarchy
        Class<?> current = eventType;
        while (current != null && EventObject.class.isAssignableFrom(current)) {
            List<EventBusListener> listeners = subscriptions.get(current);
            if (listeners != null) {
                result.addAll(listeners);
            }
            current = current.getSuperclass();
        }

        return result;
    }

    @Override
    public void subscribe(Class type, EventBusListener listener) {
        subscriptions.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
        LOG.debug("Subscribed listener for event type: {}", type.getName());
    }

    @Override
    public boolean unsubscribe(Class type, EventBusListener listener) {
        List<EventBusListener> subscriptionsForType = subscriptions.get(type);
        boolean removed = subscriptionsForType != null && subscriptionsForType.remove(listener);
        if (removed) {
            LOG.debug("Unsubscribed listener for event type: {}", type.getName());
        }
        return removed;
    }
}
