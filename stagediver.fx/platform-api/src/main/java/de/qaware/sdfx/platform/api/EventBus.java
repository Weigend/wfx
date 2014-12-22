package de.qaware.sdfx.platform.api;

import java.util.EventObject;

/**
 * The event bus for loose coupling of separate ui components.
 *
 * @param <T> the type of the Event
 * @author christian.fritz
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
     */
    void subscribe(Class type, EventBusListener<T> listener);
}
