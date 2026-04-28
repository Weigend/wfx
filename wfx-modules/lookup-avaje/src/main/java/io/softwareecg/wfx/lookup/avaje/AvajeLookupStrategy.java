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
import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.lookup.LookupStrategy;
import jakarta.annotation.Priority;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Avaje-Inject backed {@link LookupStrategy}. Replaces {@code CDILookupStrategy} for
 * projects that prefer compile-time DI over Weld/CDI.
 * <p>
 * Drop-in semantically:
 * <ul>
 *   <li>{@link #lookup(Class)}: smaller {@link Priority#value()} wins; classes without
 *       the annotation default to {@link Integer#MAX_VALUE} (lowest priority). Mirrors
 *       the existing CDILookupStrategy resolution.</li>
 *   <li>{@link #lookupAll(Class)}: descending priority order (highest first); classes
 *       without the annotation default to 0. Mirrors ServiceLoaderLookupStrategy.</li>
 * </ul>
 * <p>
 * Construction-recursion safety: Avaje builds all beans eagerly when {@link BeanScope}
 * is constructed. Lookup-time iteration touches already-constructed instances only,
 * so the recursion problem documented in {@code CDILookupStrategy.resolveOne} cannot
 * occur here. Beans that look up other beans of the same type during their constructor
 * must use {@link io.avaje.inject.BeanScope}-aware constructor parameters
 * ({@code List<T>} or {@code Provider<T>}), not {@link Lookup}.
 */
@Singleton
public class AvajeLookupStrategy implements LookupStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AvajeLookupStrategy.class);
    private static BeanScope scope;

    /**
     * Initialize the Avaje BeanScope and wire it into {@link Lookup}.
     */
    public static void initLookup() {
        initLookup(BeanScope.builder().build());
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
        List<T> candidates = scope.list(clazz);
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        return candidates.stream()
                .min(Comparator.comparingInt(c -> getPriorityValue(c.getClass())))
                .orElse(null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T lookup(TypeLiteral<T> typeLiteral) {
        // Avaje registers parameterised beans by their full Type signature
        // (e.g. "TypedTestService<java.lang.String>"). Use the Type from the
        // TypeLiteral to match — raw-class lookup would miss them.
        Type type = typeLiteral.getType();
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
    public <T> List<T> lookupAll(TypeLiteral<T> typeLiteral) {
        Type type = typeLiteral.getType();
        return (List<T>) scope.list(type).stream()
                .sorted(Comparator.comparingInt((Object o) -> getPriorityForList(o.getClass())).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Priority for single-result resolution: smaller value wins, default is MAX_VALUE.
     * Mirrors CDILookupStrategy.getPriorityValue.
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
