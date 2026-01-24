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
package de.qaware.sdfx.lookup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.util.TypeLiteral;
import java.util.List;

/**
 * The general service lookup for the platform.
 * <p>
 * It can work with the osgi registry or with google guice if this module is not loaded with osgi.
 * <p>
 * This class is thread-safe.
 *
 * @author christian.fritz
 */
@SuppressWarnings("checkstyle:com.puppycrawl.tools.checkstyle.checks.design.HideUtilityClassConstructorCheck")
public final class Lookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(Lookup.class);
    private static LookupStrategy lookupStrategy;
    private static final Object LOCK = new Object();

    /**
     * Initialize {@link de.qaware.sdfx.lookup.Lookup} with the given {@link de.qaware.sdfx.lookup.LookupStrategy}.
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
     * Init the lookup for the given class.
     *
     * @param forClazz The class which want to use the lookup.
     * @deprecated Use instead {@link de.qaware.sdfx.lookup.Lookup#init(LookupStrategy)}
     */
    @Deprecated
    @SuppressWarnings({"UtilityClassWithPublicConstructor", "PMD.UnusedFormalParameter"})
    public Lookup(Class forClazz) {
        LOGGER.warn("Do not use 'new Lookup(Class forClazz)'. Use instead 'Lookup.init(LookupStrategy)'.");
    }

    /**
     * Lookup a class from the registry.
     * <p>
     * The returned service is that service that have the highest service ranking.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A instance of the requested class or null if not found.
     */
    public static <T> T lookup(Class<T> clazz) {
        LookupStrategy strategy = getLookupStrategy();
        if (strategy != null) {
            return strategy.lookup(clazz);
        }
        return null;
    }

    /**
     * Lookup an instance from the registry. The {@link TypeLiteral} allows to return a strong typed generic instance.
     * <p>
     * The returned service is that service that have the highest service ranking.
     * <p>
     * The following example shows how to lookup a strong typed instance of {@code EventBus<ProgressEvent>} using the
     * {@link TypeLiteral}:
     * <pre>{@code
     * EventBus<ProgressEvent> eventBus = Lookup.lookup(new TypeLiteral<EventBus<ProgressEvent>>(){});
     * }</pre>
     *
     * @param type The type literal to search.
     * @param <T>  The type of the class to search.
     * @return A instance of the requested class or null if not found.
     */
    public static <T> T lookup(TypeLiteral<T> type) {
        LookupStrategy strategy = getLookupStrategy();
        if (strategy != null) {
            return strategy.lookup(type);
        }
        return null;
    }

    /**
     * Lookup all services for one class from the registry.
     * <p>
     * The list of services is ordered by the service ranking. The service with the highest ranking is the first.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A list with all found service instances for the searched class.
     */
    public static <T> List<T> lookupAll(Class<T> clazz) {
        LookupStrategy strategy = getLookupStrategy();
        if (strategy != null) {
            return strategy.lookupAll(clazz);
        }
        return null;
    }

    /**
     * Lookup all services for one class from the registry. The {@link TypeLiteral} allows to return a strong typed
     * generic instance.
     * <p>
     * The list of services is ordered by the service ranking. The service with the highest ranking is the first.
     * <p>
     * The following example shows how to lookup a strong typed instance of {@code EventBus<ProgressEvent>} using the
     * {@link TypeLiteral}:
     * <pre>{@code
     * List<EventBus<ProgressEvent>> eventBusList = strategy.lookupAll(new TypeLiteral<EventBus<ProgressEvent>>(){});
     * }</pre>
     *
     * @param type The type literal to search.
     * @param <T>  The type of the class to search.
     * @return A list with all found service instances for the searched class.
     */
    public static <T> List<T> lookupAll(TypeLiteral<T> type) {
        LookupStrategy strategy = getLookupStrategy();
        if (strategy != null) {
            return strategy.lookupAll(type);
        }
        return null;
    }

    /**
     * Get the current {@link de.qaware.sdfx.lookup.LookupStrategy}.
     *
     * @return The current lookup strategy.
     */
    public static LookupStrategy getLookupStrategy() {
        synchronized (LOCK) {
            return lookupStrategy;
        }
    }
}
