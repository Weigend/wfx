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
package io.softwareecg.wfx.platform.core.eventbus;

import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.platform.api.EventBus;
import io.softwareecg.wfx.platform.api.EventBusListener;
import io.softwareecg.wfx.platform.api.EventSubscriber;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes the {@link EventSubscriber} annotation of a given instance.
 * <p>
 * A subscription created here is a strong one: the {@link EventBus} keeps the
 * subscribed object alive until {@link #unprocess(Object)} is called. That is
 * deliberate — an object may exist only to react to events — but it means that
 * short-lived subscribers (tree items, controllers of views that get closed)
 * have to be released explicitly. WFX does that itself for the controllers of
 * FXML files loaded through the platform {@code FXMLLoader} once their view is
 * unregistered from the window manager.
 */
public final class AnnotationProcessor {

    /**
     * The subscriptions made per processed instance. Identity-based: two equal
     * but distinct instances (e.g. fresh tree items with the same id) are
     * processed independently.
     * <p>
     * Only objects that actually own a subscription are kept here. They are
     * strongly reachable through the bus anyway, so this map never extends an
     * object's lifetime on its own.
     */
    private static final Map<Object, List<Subscription>> SUBSCRIPTIONS = new IdentityHashMap<>();

    /**
     * No instantiation wanted.
     */
    private AnnotationProcessor() {
    }

    /**
     * Registers IEventBusListeners to the SimpleEventBus if the given object has
     * methods marked with the EventSubscriber annotation.
     *
     * <p>Idempotent per instance: repeated calls with the same {@code object}
     * are no-ops, so subscribing once at construction time and again from
     * other lifecycle hooks is safe.
     *
     * @param object the object to check for annotated methods
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void process(final Object object) {
        if (object == null) {
            return;
        }
        List<Method> annotated = new ArrayList<>();
        for (Method method : object.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventSubscriber.class)) {
                annotated.add(method);
            }
        }
        if (annotated.isEmpty()) {
            return;
        }
        synchronized (SUBSCRIPTIONS) {
            if (SUBSCRIPTIONS.containsKey(object)) {
                return;
            }
            EventBus eventBus = Lookup.require(EventBus.class);
            List<Subscription> subscriptions = new ArrayList<>();
            for (Method method : annotated) {
                for (Class<?> eventClass : method.getAnnotation(EventSubscriber.class).eventClass()) {
                    EventBusListener<?> listener = listenerFor(object, method);
                    eventBus.subscribe(eventClass, listener);
                    subscriptions.add(new Subscription(eventBus, eventClass, listener));
                }
            }
            SUBSCRIPTIONS.put(object, subscriptions);
        }
    }

    /**
     * Removes every subscription {@link #process(Object)} made for the given
     * object, so the event bus no longer references it.
     * <p>
     * Calling {@link #process(Object)} again afterwards subscribes the object
     * anew.
     *
     * @param object the object whose annotation-based subscriptions should end
     * @return {@code true} if the object had subscriptions that were removed
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean unprocess(final Object object) {
        List<Subscription> subscriptions;
        synchronized (SUBSCRIPTIONS) {
            subscriptions = SUBSCRIPTIONS.remove(object);
        }
        if (subscriptions == null) {
            return false;
        }
        for (Subscription subscription : subscriptions) {
            ((EventBus) subscription.bus()).unsubscribe(subscription.eventClass(), subscription.listener());
        }
        return true;
    }

    /**
     * Tells whether the given object currently holds subscriptions made by
     * {@link #process(Object)}.
     *
     * @param object the object to check
     * @return {@code true} if the object is subscribed
     */
    public static boolean isProcessed(final Object object) {
        synchronized (SUBSCRIPTIONS) {
            return SUBSCRIPTIONS.containsKey(object);
        }
    }

    private static EventBusListener<?> listenerFor(final Object object, final Method method) {
        return event -> {
            try {
                Boolean result;
                if (method.getParameterCount() == 0) {
                    result = (Boolean) method.invoke(object);
                }
                else {
                    result = (Boolean) method.invoke(object, event);
                }
                return result == null || result;
            }
            catch (ReflectiveOperationException | ClassCastException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    /**
     * One subscription made on behalf of a processed object.
     */
    private record Subscription(EventBus<?> bus, Class<?> eventClass, EventBusListener<?> listener) {
    }
}
