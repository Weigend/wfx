package de.qaware.sdfx.platform.impl.eventbus;

import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.EventBusListener;

import javax.inject.Singleton;
import java.util.*;

/**
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
}
