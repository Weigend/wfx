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
package io.softwareecg.wfx.lookup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The general service lookup for the platform.
 * <p>
 * It can work with the osgi registry or with google guice if this module is not loaded with osgi.
 * <p>
 * This class is thread-safe.
 *
 */
@SuppressWarnings("checkstyle:com.puppycrawl.tools.checkstyle.checks.design.HideUtilityClassConstructorCheck")
public final class Lookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(Lookup.class);
    private static LookupStrategy lookupStrategy;
    private static final Object LOCK = new Object();

    /**
     * Initialize {@link io.softwareecg.wfx.lookup.Lookup} with the given {@link io.softwareecg.wfx.lookup.LookupStrategy}.
     *
     * @param lookupStrategy Use this strategy to lookup for instances.
     */
    public static void init(LookupStrategy lookupStrategy) {
        synchronized (LOCK) {
            Lookup.lookupStrategy = lookupStrategy;
        }
    }

    private Lookup() {
    }

    /**
     * Lookup a class from the registry.
     * <p>
     * The returned service is that service that have the highest service ranking.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A instance of the requested class or null if not found.
     * @see #find(Class) for an Optional-returning alternative
     */
    public static <T> T lookup(Class<T> clazz) {
        LookupStrategy strategy = getLookupStrategy();
        if (strategy != null) {
            return strategy.lookup(clazz);
        }
        return null;
    }

    /**
     * Lookup a class from the registry, returning an {@link Optional}.
     * <p>
     * This is the {@link Optional}-returning alternative to {@link #lookup(Class)}.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return An Optional containing the found instance, or empty if not found.
     * @since 6.3.0
     */
    public static <T> Optional<T> find(Class<T> clazz) {
        return Optional.ofNullable(lookup(clazz));
    }

    /**
     * Lookup an instance from the registry. The {@link TypeRef} allows to return a strong typed generic instance.
     * <p>
     * The returned service is that service that have the highest service ranking.
     * <p>
     * The following example shows how to lookup a strong typed instance of {@code EventBus<ProgressEvent>} using the
     * {@link TypeRef}:
     * <pre>{@code
     * EventBus<ProgressEvent> eventBus = Lookup.lookup(new TypeRef<EventBus<ProgressEvent>>(){});
     * }</pre>
     *
     * @param type The type literal to search.
     * @param <T>  The type of the class to search.
     * @return A instance of the requested class or null if not found.
     * @see #find(TypeRef) for an Optional-returning alternative
     */
    public static <T> T lookup(TypeRef<T> type) {
        LookupStrategy strategy = getLookupStrategy();
        if (strategy != null) {
            return strategy.lookup(type);
        }
        return null;
    }

    /**
     * Lookup an instance from the registry using a TypeRef, returning an {@link Optional}.
     * <p>
     * This is the {@link Optional}-returning alternative to {@link #lookup(TypeRef)}.
     *
     * @param type The type literal to search.
     * @param <T>  The type of the class to search.
     * @return An Optional containing the found instance, or empty if not found.
     * @since 6.3.0
     */
    public static <T> Optional<T> find(TypeRef<T> type) {
        return Optional.ofNullable(lookup(type));
    }

    /**
     * Lookup all services for one class from the registry.
     * <p>
     * The list of services is ordered by the service ranking. The service with the highest ranking is the first.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A list with all found service instances for the searched class, or null if no strategy is set.
     * @see #findAll(Class) for an empty-list-returning alternative
     */
    public static <T> List<T> lookupAll(Class<T> clazz) {
        LookupStrategy strategy = getLookupStrategy();
        if (strategy != null) {
            return strategy.lookupAll(clazz);
        }
        return null;
    }

    /**
     * Lookup all services for one class from the registry.
     * <p>
     * Unlike {@link #lookupAll(Class)}, this method returns an empty list instead of null
     * when no services are found or no strategy is set.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A list with all found service instances, never null.
     * @since 6.3.0
     */
    public static <T> List<T> findAll(Class<T> clazz) {
        List<T> result = lookupAll(clazz);
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Lookup all services for one class from the registry. The {@link TypeRef} allows to return a strong typed
     * generic instance.
     * <p>
     * The list of services is ordered by the service ranking. The service with the highest ranking is the first.
     * <p>
     * The following example shows how to lookup a strong typed instance of {@code EventBus<ProgressEvent>} using the
     * {@link TypeRef}:
     * <pre>{@code
     * List<EventBus<ProgressEvent>> eventBusList = strategy.lookupAll(new TypeRef<EventBus<ProgressEvent>>(){});
     * }</pre>
     *
     * @param type The type literal to search.
     * @param <T>  The type of the class to search.
     * @return A list with all found service instances for the searched class, or null if no strategy is set.
     * @see #findAll(TypeRef) for an empty-list-returning alternative
     */
    public static <T> List<T> lookupAll(TypeRef<T> type) {
        LookupStrategy strategy = getLookupStrategy();
        if (strategy != null) {
            return strategy.lookupAll(type);
        }
        return null;
    }

    /**
     * Lookup all services using a TypeRef from the registry.
     * <p>
     * Unlike {@link #lookupAll(TypeRef)}, this method returns an empty list instead of null
     * when no services are found or no strategy is set.
     *
     * @param type The type literal to search.
     * @param <T>  The type of the class to search.
     * @return A list with all found service instances, never null.
     * @since 6.3.0
     */
    public static <T> List<T> findAll(TypeRef<T> type) {
        List<T> result = lookupAll(type);
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Get the current {@link io.softwareecg.wfx.lookup.LookupStrategy}.
     *
     * @return The current lookup strategy.
     */
    public static LookupStrategy getLookupStrategy() {
        synchronized (LOCK) {
            return lookupStrategy;
        }
    }
}
