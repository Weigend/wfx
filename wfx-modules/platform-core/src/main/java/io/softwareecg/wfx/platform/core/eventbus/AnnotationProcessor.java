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
import io.softwareecg.wfx.platform.api.EventSubscriber;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Processes the {@link EventSubscriber} annotation of a given instance.
 *
 */
public final class AnnotationProcessor {

    /**
     * Tracks instances that have already been processed so a second
     * {@link #process(Object)} call on the same object does not subscribe its
     * methods to the {@link EventBus} a second time. Identity-based: two equal
     * but distinct instances (e.g. fresh tree items with the same id) are
     * processed independently.
     */
    private static final Set<Object> PROCESSED =
            Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));

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
        if (object == null || !PROCESSED.add(object)) {
            return;
        }
        Class<?> clazz = object.getClass();
        Method[] methods = clazz.getMethods();
        for (final Method method : methods) {
            if (method.isAnnotationPresent(EventSubscriber.class)) {
                EventSubscriber s = method.getAnnotation(EventSubscriber.class);
                for (Class<?> eventClass : s.eventClass()) {
                    EventBus eventBus = Lookup.lookup(EventBus.class);
                    eventBus.subscribe(eventClass, event -> {
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
                    });
                }
            }
        }
    }
}
