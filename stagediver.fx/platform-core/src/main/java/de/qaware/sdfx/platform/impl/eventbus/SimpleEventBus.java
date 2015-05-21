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
package de.qaware.sdfx.platform.impl.eventbus;

import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.EventBusListener;

import javax.inject.Singleton;
import java.util.*;

/**
 * A very simple event bus.
 *
 * @author christian.fritz
 */
@Singleton
public class SimpleEventBus implements EventBus {
    private Map<Class, List<EventBusListener>> subscriptions = new WeakHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public boolean publish(EventObject event) {
        List<EventBusListener> subscriptionsForType = subscriptions.get(event.getClass());
        boolean result = true;
        if (subscriptionsForType != null) {
            for (EventBusListener next : subscriptionsForType) {
                result &= next.eventPublished(event);
            }
        }
        return result;
    }

    @Override
    public void subscribe(Class type, EventBusListener listener) {
        List<EventBusListener> subscriptionsForType = subscriptions.get(type);
        if (subscriptionsForType == null) {
            subscriptionsForType = new ArrayList<>();
            subscriptions.put(type, subscriptionsForType);
        }
        subscriptionsForType.add(listener);
    }

    @Override
    public boolean unsubscribe(Class type, EventBusListener listener) {
        List<EventBusListener> subscriptionsForType = subscriptions.get(type);
        return subscriptionsForType != null && subscriptionsForType.remove(listener);
    }
}
