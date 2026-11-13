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
import io.softwareecg.wfx.lookup.api.Priorities;
import io.softwareecg.wfx.lookup.api.TypeRef;
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Avaje-Inject backed {@link LookupStrategy}.
 * <p>
 * {@link Priority @Priority} is read by the one WFX-wide rule in
 * {@link Priorities}: a smaller value takes precedence, unannotated classes
 * come last.
 * <ul>
 *   <li>{@link #lookupAll(Class)}: all beans of the type in that order.</li>
 *   <li>{@link #lookup(Class)}: the bean Avaje itself would inject. That is
 *       the first of {@code lookupAll}, with one refinement only the container
 *       knows about: {@link io.avaje.inject.Primary @Primary} wins over a
 *       plain bean, which wins over {@link io.avaje.inject.Secondary
 *       @Secondary}. Avaje refuses to choose between equally ranked beans
 *       (it throws {@link IllegalStateException}); the strategy then picks
 *       by {@code @Priority}.</li>
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
            // Avaje refuses to disambiguate (e.g. two plain beans for the
            // same type). Pick by @Priority, i.e. the head of lookupAll.
            List<T> candidates = scope.list(clazz).stream()
                    .sorted(Priorities.comparator())
                    .collect(Collectors.toList());
            if (candidates.isEmpty()) {
                return null;
            }
            T chosen = candidates.get(0);
            if (candidates.size() > 1 && Priorities.of(chosen) == Priorities.of(candidates.get(1))) {
                // Not even @Priority tells the candidates apart: the pick
                // depends on registration order. Worth telling the developer.
                LOGGER.warn("{} beans of type {} are equally eligible; using {}. "
                                + "Declare a @Priority (smaller value wins) or mark one @Primary.",
                        candidates.size(), clazz.getName(), chosen.getClass().getName());
            }
            else {
                LOGGER.debug("Several beans of type {}; {} has precedence by @Priority.",
                        clazz.getName(), chosen.getClass().getName());
            }
            return chosen;
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
                .min(Priorities.comparator())
                .orElse(null);
    }

    @Override
    public <T> List<T> lookupAll(Class<T> clazz) {
        return scope.list(clazz).stream()
                .sorted(Priorities.comparator())
                .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> lookupAll(TypeRef<T> typeRef) {
        Type type = typeRef.getType();
        return (List<T>) scope.list(type).stream()
                .sorted(Priorities.comparator())
                .collect(Collectors.toList());
    }

    static BeanScope getScope() {
        return scope;
    }

    static void setScope(BeanScope newScope) {
        scope = newScope;
    }
}
