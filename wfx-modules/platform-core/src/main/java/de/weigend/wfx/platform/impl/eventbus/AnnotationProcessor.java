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

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.platform.api.EventBus;
import de.weigend.wfx.platform.api.EventSubscriber;

import java.lang.reflect.Method;

/**
 * Processes the {@link EventSubscriber} annotation of a given instance.
 *
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void process(final Object object) {
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
