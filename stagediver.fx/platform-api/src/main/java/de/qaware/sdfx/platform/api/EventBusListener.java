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
