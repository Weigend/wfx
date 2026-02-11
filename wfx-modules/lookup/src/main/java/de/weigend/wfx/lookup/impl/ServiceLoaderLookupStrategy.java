/*
 * #%L
 * The core lookup module of the wfx platform and all applications.
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
package de.weigend.wfx.lookup.impl;

import de.weigend.wfx.lookup.LookupStrategy;
import jakarta.enterprise.util.TypeLiteral;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Use the javas {@link java.util.ServiceLoader} to lookup the actual instances.
 *
 * @author Software-EKG Team
 */
public class ServiceLoaderLookupStrategy implements LookupStrategy {

    private final Map<Class<?>, List<Object>> lookupCache = new ConcurrentHashMap<>();

    /**
     * Initializes the internal structure to be able to lookup services.
     *
     * @param o the 2d-Array with the classes mapped to services
     */
    @SuppressWarnings("unchecked")
    public void init(Object[][] o) {
        for (Object[] objects : o) {
            Class<?> clazz = (Class<?>) objects[0];
            Object value = objects[1];
            initRaw(clazz, value);
        }
    }

    /**
     * Internal method for raw class initialization (used by Object[][] init).
     */
    private void initRaw(Class<?> clazz, Object instance) {
        lookupCache.computeIfAbsent(clazz, k -> new ArrayList<>()).add(instance);
    }

    /**
     * Add a new instance to the lookup. Existing instances are not overridden.
     *
     * @param clazz    The target class.
     * @param instance The added instance.
     * @param <T>      Type of the target instance.
     */
    public <T> void init(Class<T> clazz, T instance) {
        init(clazz, instance, false);
    }

    /**
     * Add a new instance to the lookup. Existing instances are not overridden.
     *
     * @param type     The target type.
     * @param instance The added instance.
     * @param <T>      Type of the target instance.
     */
    public <T> void init(TypeLiteral<T> type, T instance) {
        init(type.getRawType(), instance);
    }

    /**
     * Add a new instance to the lookup. If override is true, all existing instances are removed previous.
     *
     * @param clazz    The target class.
     * @param instance The added instance.
     * @param override True if existing instances should be removed previous.
     * @param <T>      Type of the target instance.
     */
    public <T> void init(Class<T> clazz, T instance, boolean override) {
        if (override) {
            lookupCache.remove(clazz);
        }
        lookupCache.computeIfAbsent(clazz, k -> new ArrayList<>()).add(instance);
    }

    /**
     * Add a new instance to the lookup. If override is true, all existing instances are removed previous.
     *
     * @param type     The target type.
     * @param instance The added instance.
     * @param override True if existing instances should be removed previous.
     * @param <T>      Type of the target instance.
     */
    public <T> void init(TypeLiteral<T> type, T instance, boolean override) {
        init(type.getRawType(), instance, override);
    }

    /**
     * Add a new producer for the given class. Existing instances are not overridden.
     *
     * @param clazz    The target class.
     * @param producer The producer instance.
     * @param <T>      Type of the target instance.
     */
    public <T> void init(Class<T> clazz, Producer<T> producer) {
        init(clazz, producer, false);
    }

    /**
     * Add a new producer for the given class. Existing instances are not overridden.
     *
     * @param type     The target type.
     * @param producer The producer instance.
     * @param <T>      Type of the target instance.
     */
    public <T> void init(TypeLiteral<T> type, Producer<T> producer) {
        init(type.getRawType(), producer);
    }

    /**
     * Add a new producer for the given class. If override is true, all existing instances are removed previous.
     *
     * @param clazz    The target class.
     * @param producer The producer instance.
     * @param override True if existing instances should be removed previous.
     * @param <T>      Type of the target instance.
     */
    public <T> void init(Class<T> clazz, Producer<T> producer, boolean override) {
        if (override) {
            lookupCache.remove(clazz);
        }
        lookupCache.computeIfAbsent(clazz, k -> new ArrayList<>()).add(producer);
    }

    /**
     * Add a new producer for the given class. If override is true, all existing instances are removed previous.
     *
     * @param type     The target type.
     * @param producer The producer instance.
     * @param override True if existing instances should be removed previous.
     * @param <T>      Type of the target instance.
     */
    public <T> void init(TypeLiteral<T> type, Producer<T> producer, boolean override) {
        init(type.getRawType(), producer, override);
    }

    @Override
    public <T> T lookup(TypeLiteral<T> type) {
        return lookup(type.getRawType());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T lookup(Class<T> clazz) {
        if (!lookupCache.containsKey(clazz)) {
            loadInstances(clazz);
        }
        List<Object> objects = lookupCache.getOrDefault(clazz, List.of());
        if (objects.isEmpty()) {
            return null;
        }
        Object first = objects.get(0);
        if (first instanceof Producer) {
            return ((Producer<T>) first).getInstance();
        }
        return (T) first;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> lookupAll(Class<T> clazz) {
        if (!lookupCache.containsKey(clazz)) {
            loadInstances(clazz);
        }
        List<Object> cached = lookupCache.getOrDefault(clazz, List.of());
        return cached.stream()
                .map(object -> {
                    if (object instanceof Producer) {
                        return ((Producer<T>) object).getInstance();
                    }
                    return (T) object;
                })
                .sorted(Comparator.comparingInt((T o) -> getPriority(o.getClass())).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get the priority for a specified class.
     * <p>
     * First the value of {@link jakarta.annotation.Priority} is taken. If no
     * {@link jakarta.annotation.Priority} is available it returns 0.
     *
     * @param c Determ the priority for this class.
     * @return The priority determed for the given class.
     */
    public static int getPriority(Class<?> c) {
        int priority = 0;
        if (c.isAnnotationPresent(jakarta.annotation.Priority.class)) {
            priority = c.getAnnotation(jakarta.annotation.Priority.class).value();
        }
        return priority;
    }

    @Override
    public <T> List<T> lookupAll(TypeLiteral<T> type) {
        return lookupAll(type.getRawType());
    }

    /**
     * Load the instances with the jdk's {@link java.util.ServiceLoader}.
     *
     * @param clazz The target class.
     * @param <T>   The type of the target class.
     */
    private <T> void loadInstances(Class<T> clazz) {
        ServiceLoader<T> load = ServiceLoader.load(clazz);
        List<Object> instances = lookupCache.computeIfAbsent(clazz, k -> new ArrayList<>());
        for (T instance : load) {
            instances.add(instance);
        }
    }

    /**
     * Producer to create the instance created by the lookup strategy.
     * <p>
     * This producers can only be used with the {@link de.weigend.wfx.lookup.impl.ServiceLoaderLookupStrategy}.
     *
     * @param <T> The type of the created instance.
     */
    public interface Producer<T> {

        /**
         * Produces the instance.
         *
         * @return The created instance.
         */
        T getInstance();
    }
}
