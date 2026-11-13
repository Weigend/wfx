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
package io.softwareecg.wfx.lookup.serviceloader;

import io.softwareecg.wfx.lookup.api.LookupStrategy;
import io.softwareecg.wfx.lookup.api.Priorities;
import io.softwareecg.wfx.lookup.api.TypeRef;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Use the javas {@link java.util.ServiceLoader} to lookup the actual instances.
 * <p>
 * {@link jakarta.annotation.Priority @Priority} is read by the one WFX-wide
 * rule in {@link Priorities}: {@link #lookupAll(Class)} lists the instance
 * with the smaller value first, {@link #lookup(Class)} returns the first of
 * that list.
 * <p>
 * The strategy's own priority makes it the fallback: {@code Lookup.init()}
 * prefers a DI-backed strategy when one is on the classpath.
 */
@jakarta.annotation.Priority(1000)
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
    public <T> void init(TypeRef<T> type, T instance) {
        initRaw(type.getRawType(), instance);
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
    public <T> void init(TypeRef<T> type, T instance, boolean override) {
        if (override) {
            lookupCache.remove(type.getRawType());
        }
        initRaw(type.getRawType(), instance);
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
    public <T> void init(TypeRef<T> type, Producer<T> producer) {
        initRaw(type.getRawType(), producer);
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
    public <T> void init(TypeRef<T> type, Producer<T> producer, boolean override) {
        if (override) {
            lookupCache.remove(type.getRawType());
        }
        initRaw(type.getRawType(), producer);
    }

    @Override
    public <T> T lookup(TypeRef<T> type) {
        return lookup((Class<T>) type.getRawType());
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
        if (objects.size() > 1) {
            // Several candidates: the one with precedence is the head of
            // lookupAll, so lookup(X) and lookupAll(X).get(0) always agree.
            return lookupAll(clazz).get(0);
        }
        Object only = objects.get(0);
        if (only instanceof Producer) {
            return ((Producer<T>) only).getInstance();
        }
        return (T) only;
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
                .sorted(Priorities.comparator())
                .collect(Collectors.toList());
    }

    /**
     * Get the priority for a specified class.
     *
     * @param c Determ the priority for this class.
     * @return The priority determed for the given class.
     * @deprecated use {@link Priorities#of(Class)}, the single place that
     *             defines how WFX reads {@link jakarta.annotation.Priority}.
     *             Note that an unannotated class now has the priority
     *             {@link Priorities#DEFAULT} (last) instead of 0.
     */
    @Deprecated
    public static int getPriority(Class<?> c) {
        return Priorities.of(c);
    }

    @Override
    public <T> List<T> lookupAll(TypeRef<T> type) {
        return lookupAll((Class<T>) type.getRawType());
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
     * This producers can only be used with the {@link io.softwareecg.wfx.lookup.impl.ServiceLoaderLookupStrategy}.
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
