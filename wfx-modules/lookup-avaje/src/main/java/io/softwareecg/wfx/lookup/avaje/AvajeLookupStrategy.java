/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2026 Weigend AM
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
package io.softwareecg.wfx.lookup.avaje;

import io.avaje.inject.BeanScope;
import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.lookup.api.LookupStrategy;
import io.softwareecg.wfx.lookup.api.TypeRef;
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Avaje-Inject backed {@link LookupStrategy}.
 * <p>
 * Drop-in semantically:
 * <ul>
 *   <li>{@link #lookup(Class)}: defers to Avaje's native single-bean resolution,
 *       which honours {@link io.avaje.inject.Primary @Primary} (wins over plain),
 *       plain {@code @Singleton} (wins over secondary), and
 *       {@link io.avaje.inject.Secondary @Secondary} (fallback). Two equally-
 *       ranked beans (e.g. two plain @Singletons or two @Primary) make Avaje
 *       throw {@link IllegalStateException}; the strategy then falls back to
 *       {@link Priority @Priority} sort (smaller value wins; default
 *       {@link Integer#MAX_VALUE}) so consumers with that configuration keep
 *       working.</li>
 *   <li>{@link #lookupAll(Class)}: descending priority order (highest first); classes
 *       without the annotation default to 0. Mirrors ServiceLoaderLookupStrategy.</li>
 * </ul>
 * <p>
 * Construction-recursion safety: Avaje builds all beans eagerly when {@link BeanScope}
 * is constructed. Lookup-time iteration touches already-constructed instances only,
 * so lookup-time iteration does not construct new matching instances. Beans
 * that look up other beans of the same type during their constructor
 * must use {@link io.avaje.inject.BeanScope}-aware constructor parameters
 * ({@code List<T>} or {@code Provider<T>}), not {@link Lookup}.
 */
@Singleton
@Priority(100)
public class AvajeLookupStrategy implements LookupStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvajeLookupStrategy.class);
    private static BeanScope scope;
    private static volatile boolean initializing = false;

    /**
     * Called by {@link java.util.ServiceLoader} when discovered via META-INF/services.
     * Triggers avaje bootstrap so {@link Lookup#init()} auto-wires without an explicit call.
     * The {@code initializing} guard prevents the avaje-internal bean construction
     * (which also calls this constructor) from re-entering {@link #initLookup()}.
     */
    public AvajeLookupStrategy() {
        if (scope == null && !initializing) {
            initializing = true;
            try {
                initLookup();
            } finally {
                initializing = false;
            }
        }
    }

    /**
     * Initialize the Avaje BeanScope and wire it into {@link Lookup}.
     */
    public static void initLookup() {
        // Diagnostic: dump every Avaje module ServiceLoader sees, so a missing
        // multi-module wiring is obvious in the startup log. Use this class's
        // classloader (NOT the thread-context one, which under JavaFX init may
        // be a delegating loader that does not see our application jars).
        ClassLoader cl = AvajeLookupStrategy.class.getClassLoader();
        var modules = java.util.ServiceLoader.load(io.avaje.inject.spi.InjectExtension.class, cl);
        StringBuilder list = new StringBuilder();
        for (io.avaje.inject.spi.InjectExtension m : modules) {
            if (list.length() > 0) list.append(", ");
            list.append(m.getClass().getName());
        }
        LOGGER.info("Avaje InjectExtensions discovered via ServiceLoader: [{}]", list);
        initLookup(BeanScope.builder().classLoader(cl).build());
    }

    /**
     * Initialize with an externally built {@link BeanScope}. Useful for tests that
     * want to register additional beans, or for application bootstraps that build
     * the scope with custom modules.
     *
     * @param externalScope a fully constructed BeanScope
     */
    public static void initLookup(BeanScope externalScope) {
        scope = externalScope;
        Lookup.init(scope.get(AvajeLookupStrategy.class));
        LOGGER.info("Successfully initialized Avaje and Lookup");
    }

    @Override
    public void shutdown() {
        shutdownLookup();
    }

    /**
     * Shutdown the Avaje BeanScope.
     */
    public static void shutdownLookup() {
        if (scope != null) {
            try {
                LOGGER.info("Shutting down Avaje BeanScope...");
                scope.close();
                LOGGER.info("Avaje BeanScope successfully shut down");
            } catch (Exception e) {
                LOGGER.error("Error shutting down Avaje BeanScope", e);
            } finally {
                scope = null;
            }
        }
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        // Avaje does not auto-register its own BeanScope as an injectable bean.
        // Expose it explicitly so SDK helpers like AvajeInjection can locate
        // the scope through Lookup without taking a hard dependency on
        // lookup-avaje.
        if (clazz == BeanScope.class) {
            return clazz.cast(scope);
        }
        // Defer to Avaje's native single-bean resolution: it correctly applies
        // @Primary > regular > @Secondary precedence via DContextEntry's
        // EntryMatcher.checkMatch. Walking scope.list and sorting by @Priority
        // here would silently break @Secondary — both the regular and the
        // @Secondary bean carry no @Priority and would tie at MAX_VALUE,
        // letting registration order pick the winner (which is the WFX
        // default, not the application's override).
        try {
            return scope.getOptional(clazz).orElse(null);
        }
        catch (IllegalStateException ambiguous) {
            // Avaje refuses to disambiguate (e.g. two @Primary beans for the
            // same type). Fall back to the legacy @Priority-based tiebreaker
            // — smaller value wins — so consumers that relied on it keep
            // working.
            LOGGER.warn("Avaje could not resolve a unique bean for {}; falling back to @Priority sort.",
                    clazz.getName(), ambiguous);
            List<T> candidates = scope.list(clazz);
            return candidates.stream()
                    .min(Comparator.comparingInt(c -> getPriorityValue(c.getClass())))
                    .orElse(null);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T lookup(TypeRef<T> typeRef) {
        // Avaje registers parameterised beans by their full Type signature
        // (e.g. "TypedTestService<java.lang.String>"). Use the Type from the
        // TypeRef to match; raw-class lookup would miss them.
        Type type = typeRef.getType();
        List<?> candidates = scope.list(type);
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return (T) candidates.get(0);
        }
        return (T) candidates.stream()
                .min(Comparator.comparingInt(c -> getPriorityValue(c.getClass())))
                .orElse(null);
    }

    @Override
    public <T> List<T> lookupAll(Class<T> clazz) {
        return scope.list(clazz).stream()
                .sorted(Comparator.comparingInt((T t) -> getPriorityForList(t.getClass())).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> lookupAll(TypeRef<T> typeRef) {
        Type type = typeRef.getType();
        return (List<T>) scope.list(type).stream()
                .sorted(Comparator.comparingInt((Object o) -> getPriorityForList(o.getClass())).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Priority for single-result resolution: smaller value wins, default is MAX_VALUE.
     * Mirrors WFX single-result priority resolution.
     */
    private static int getPriorityValue(Class<?> c) {
        Priority p = c.getAnnotation(Priority.class);
        return p != null ? p.value() : Integer.MAX_VALUE;
    }

    /**
     * Priority for list ordering: higher value comes first, default is 0.
     * Mirrors ServiceLoaderLookupStrategy.getPriority.
     */
    private static int getPriorityForList(Class<?> c) {
        Priority p = c.getAnnotation(Priority.class);
        return p != null ? p.value() : 0;
    }

    static BeanScope getScope() {
        return scope;
    }

    static void setScope(BeanScope newScope) {
        scope = newScope;
    }
}
