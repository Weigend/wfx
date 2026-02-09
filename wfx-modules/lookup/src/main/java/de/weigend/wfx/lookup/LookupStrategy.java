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
package de.weigend.wfx.lookup;

import jakarta.enterprise.util.TypeLiteral;
import java.util.List;

/**
 * The {@code LookupStrategy} is used by the {@link de.weigend.wfx.lookup.Lookup} class to get a concrete instance of
 * of the given {@link java.lang.Class} object. It allows you to use our own registry, service locator or dependency
 * injection container.
 *
 * @author Software-EKG Team
 */
public interface LookupStrategy {
    /**
     * Lookup an instance from the registry. The {@link TypeLiteral} allows to return a strong typed generic instance.
     * <p>
     * The returned service is that service that have the highest service ranking.
     * <p>
     * The following example shows how to lookup a strong typed instance of {@code EventBus<ProgressEvent>} using the
     * {@link TypeLiteral}:
     * <pre>{@code
     * EventBus<ProgressEvent> eventBus = strategy.lookup(new TypeLiteral<EventBus<ProgressEvent>>(){});
     * }</pre>
     *
     * @param type The type literal to search.
     * @param <T>  The type of the class to search.
     * @return A instance of the requested class or null if not found.
     */
    <T> T lookup(TypeLiteral<T> type);

    /**
     * Lookup an instance from the registry.
     * <p>
     * The returned service is that service that have the highest service ranking.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A instance of the requested class or null if not found.
     */
    <T> T lookup(Class<T> clazz);

    /**
     * Lookup all services for one class from the registry.
     * <p>
     * The list of services is ordered by the service ranking. The service with the highest ranking is the first.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A list with all found service instances for the searched class.
     */
    <T> List<T> lookupAll(Class<T> clazz);

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
    <T> List<T> lookupAll(TypeLiteral<T> type);
}
