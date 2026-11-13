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
package io.softwareecg.wfx.lookup.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * The general service lookup for the platform.
 * <p>
 * A service locator: it resolves an interface at the moment of the call rather
 * than when the caller is constructed, and it is available to objects no
 * container creates — FXML controllers, views, tree items. The actual
 * resolution is delegated to a pluggable {@link LookupStrategy} (Avaje Inject
 * or the Java {@link ServiceLoader}).
 * <p>
 * Three ways to ask for a single service:
 * <ul>
 *     <li>{@link #lookup(Class)} returns {@code null} when there is none,</li>
 *     <li>{@link #find(Class)} wraps that in an {@link Optional},</li>
 *     <li>{@link #require(Class)} fails with a descriptive exception — use it
 *     for services the caller cannot work without.</li>
 * </ul>
 * When several implementations exist, {@link #lookupAll(Class)} returns them
 * in order of precedence and {@link #lookup(Class)} returns the first of that
 * order. See {@link Priorities} for how {@code @Priority} determines it.
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
     * Auto-initialize {@link Lookup} by discovering a {@link LookupStrategy} via {@link ServiceLoader}.
     * Works in both JPMS (provides/uses) and classpath (META-INF/services) environments.
     * <p>
     * The strategy's constructor may itself call {@link #init(LookupStrategy)} (e.g. avaje
     * initializes the DI scope and registers its own managed instance during construction).
     * In that case the strategy is already set before {@code ifPresent} runs, so we do not
     * overwrite it with the ServiceLoader-created bootstrap instance.
     */
    public static void init() {
        ServiceLoader.load(LookupStrategy.class).stream()
                .map(ServiceLoader.Provider::get)
                .min(Priorities.comparator())
                .ifPresent(s -> {
                    if (getLookupStrategy() == null) {
                        init(s);
                    }
                });
    }

    /**
     * Initialize {@link io.softwareecg.wfx.lookup.api.Lookup} with the given {@link io.softwareecg.wfx.lookup.api.LookupStrategy}.
     *
     * @param lookupStrategy Use this strategy to lookup for instances.
     */
    public static void init(LookupStrategy lookupStrategy) {
        synchronized (LOCK) {
            Lookup.lookupStrategy = lookupStrategy;
        }
    }

    /**
     * Shuts down the current {@link LookupStrategy} and clears the lookup registry.
     * Intended for use in tests to reset state between test runs.
     */
    public static void shutdown() {
        LookupStrategy strategy;
        synchronized (LOCK) {
            strategy = Lookup.lookupStrategy;
            Lookup.lookupStrategy = null;
        }
        if (strategy != null) {
            strategy.shutdown();
        }
    }

    private Lookup() {
    }

    /**
     * Lookup a class from the registry.
     * <p>
     * If several implementations are registered, the one with precedence is
     * returned — the first element of {@link #lookupAll(Class)}.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A instance of the requested class or null if not found.
     * @see #find(Class) for an Optional-returning alternative
     * @see #require(Class) for an alternative that fails instead of returning null
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
     * Lookup a class from the registry and fail if there is none.
     * <p>
     * Use this instead of {@link #lookup(Class)} for a service the caller
     * cannot do without: a missing registration then surfaces right here, with
     * the name of the missing service, instead of as a
     * {@link NullPointerException} somewhere downstream.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return The instance of the requested class, never null.
     * @throws IllegalStateException if the lookup is not initialised or no
     *                               implementation of the class is registered.
     */
    public static <T> T require(Class<T> clazz) {
        T service = lookup(clazz);
        if (service == null) {
            throw missing(clazz.getName());
        }
        return service;
    }

    /**
     * Lookup an instance from the registry using a TypeRef and fail if there is none.
     *
     * @param type The type literal to search.
     * @param <T>  The type of the class to search.
     * @return The instance of the requested type, never null.
     * @throws IllegalStateException if the lookup is not initialised or no
     *                               implementation of the type is registered.
     * @see #require(Class)
     */
    public static <T> T require(TypeRef<T> type) {
        T service = lookup(type);
        if (service == null) {
            throw missing(type.getType().getTypeName());
        }
        return service;
    }

    private static IllegalStateException missing(String typeName) {
        LookupStrategy strategy = getLookupStrategy();
        if (strategy == null) {
            return new IllegalStateException("Cannot look up " + typeName
                    + ": the Lookup is not initialised. Call Lookup.init() first.");
        }
        return new IllegalStateException("No implementation of " + typeName
                + " is registered with the Lookup (strategy: " + strategy.getClass().getName() + ").");
    }

    /**
     * Lookup an instance from the registry. The {@link TypeRef} allows to return a strong typed generic instance.
     * <p>
     * If several implementations are registered, the one with precedence is
     * returned — the first element of {@link #lookupAll(TypeRef)}.
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
     * The list is in order of precedence, see {@link Priorities}: the service
     * with the smallest {@code @Priority} value is the first.
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
     * The list is in order of precedence, see {@link Priorities}: the service
     * with the smallest {@code @Priority} value is the first.
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
     * Get the current {@link io.softwareecg.wfx.lookup.api.LookupStrategy}.
     *
     * @return The current lookup strategy.
     */
    public static LookupStrategy getLookupStrategy() {
        synchronized (LOCK) {
            return lookupStrategy;
        }
    }
}
