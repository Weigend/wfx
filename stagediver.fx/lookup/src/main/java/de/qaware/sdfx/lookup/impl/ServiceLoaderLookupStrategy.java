/*
 * #%L
 * The core lookup module of the stagediver.fx platform and all applications.
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
package de.qaware.sdfx.lookup.impl;

import com.google.common.collect.ArrayListMultimap;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.lookup.Priority;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Use the javas {@link java.util.ServiceLoader} to lookup the actual instances.
 *
 * @author christian.fritz
 */
public class ServiceLoaderLookupStrategy implements LookupStrategy {

    private ArrayListMultimap<Class, Object> lookupCache = ArrayListMultimap.create();

    /**
     * Initializes the internal structure to be able to lookup services.
     *
     * @param o the 2d-Array with the classes mapped to services
     */
    @SuppressWarnings("unchecked")
    public void init(Object[][] o) {
        for (Object[] objects : o) {
            Class clazz = (Class) objects[0];
            Object value = objects[1];
            init(clazz, value);
        }
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
     * Add a new instance to the lookup. If override is true, all existing instances are removed previous.
     *
     * @param clazz    The target class.
     * @param instance The added instance.
     * @param override True if existing instances should be removed previous.
     * @param <T>      Type of the target instance.
     */
    public <T> void init(Class<T> clazz, T instance, boolean override) {
        if (override) {
            lookupCache.removeAll(clazz);
        }
        lookupCache.put(clazz, instance);
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
     * Add a new producer for the given class. If override is true, all existing instances are removed previous.
     *
     * @param clazz    The target class.
     * @param producer The producer instance.
     * @param override True if existing instances should be removed previous.
     * @param <T>      Type of the target instance.
     */
    public <T> void init(Class<T> clazz, Producer<T> producer, boolean override) {
        if (override) {
            lookupCache.removeAll(clazz);
        }
        lookupCache.put(clazz, producer);
    }

    @Override
    public <T> T lookup(TypeLiteral<T> type) {
        return lookup(getType(type));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T lookup(Class<T> clazz) {
        if (!lookupCache.containsKey(clazz)) {
            loadInstances(clazz);
        }
        List<T> objects = (List<T>) lookupCache.get(clazz);
        if (objects.size() <= 0) {
            return null;
        }
        if (objects.get(0) instanceof Producer) {
            return ((Producer<T>) objects.get(0)).getInstance();
        }
        return objects.get(0);

    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> lookupAll(Class<T> clazz) {
        if (!lookupCache.containsKey(clazz)) {
            loadInstances(clazz);
        }
        List<Pair<T, Integer>> instances = new ArrayList<>();
        for (Object object : lookupCache.get(clazz)) {
            T instance;
            if (object instanceof Producer) {
                instance = ((Producer<T>) object).getInstance();
            }
            else {
                instance = (T) object;
            }
            Class<?> c = instance.getClass();
            instances.add(Pair.of(instance,
                    c.isAnnotationPresent(Priority.class) ? c.getAnnotation(Priority.class).value() : 0));
        }
        return instances.stream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public <T> List<T> lookupAll(TypeLiteral<T> type) {
        return lookupAll(getType(type));
    }

    /**
     * Load the instances with the jdk's {@link java.util.ServiceLoader}.
     *
     * @param clazz The target class.
     * @param <T>   The type of the target class.
     */
    private <T> void loadInstances(Class<T> clazz) {
        ServiceLoader<T> load = ServiceLoader.load(clazz);
        for (T instance : load) {
            lookupCache.put(clazz, instance);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getType(TypeLiteral<T> typeLiteral) {
        Type type = typeLiteral.getType();
        if (type instanceof Class) {
            return (Class<T>) type;
        }
        else if (type instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) type).getRawType();
        }
        else if (type instanceof GenericArrayType) {
            return (Class<T>) Object[].class;
        }
        else {
            throw new IllegalArgumentException("Illegal type");
        }
    }

    /**
     * Producer to create the instance created by the lookup strategy.
     * <p/>
     * This producers can only be used with the {@link de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy}.
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
