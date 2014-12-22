package de.qaware.sdfx.platform.impl.eventbus;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.EventSubscriber;

import java.lang.reflect.Method;

/**
 * @author christian.fritz
 */
public final class AnnotationProcessor {

    /**
     * No instantiation wanted.
     */
    private AnnotationProcessor() {
    }

    /**
     * Registers IEventBusListeners to the SimpleEventBus if the given object has
     * methods marked with the EventSubscriber annotation.
     *
     * @param object the object to check for annotated methods
     */
    @SuppressWarnings("unchecked")
    public static void process(final Object object) {
        Class clazz = object.getClass();
        Method[] methods = clazz.getMethods();
        for (final Method method : methods) {
            if (method.isAnnotationPresent(EventSubscriber.class)) {
                EventSubscriber s = method.getAnnotation(EventSubscriber.class);
                Lookup.lookup(EventBus.class).subscribe(s.eventClass(), event -> {
                    try {
                        return (Boolean) method.invoke(object, event);
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                });
            }
        }
    }
}
