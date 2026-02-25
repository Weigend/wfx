/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
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
package de.weigend.wfx.lookup.cdi;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.lookup.LookupStrategy;
import de.weigend.wfx.lookup.impl.ServiceLoaderLookupStrategy;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Using Contexts and Dependency Injection (CDI) as lookup strategy.
 *
 */
@Singleton
public class CDILookupStrategy implements LookupStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDILookupStrategy.class);
    private static Weld weld;

    @ Inject
    private Instance<Object> weldInstance;

    /**
     * Initialize the Lookup module.
     */
    public static void initLookup() {
        weld = new Weld(RegistrySingletonProvider.STATIC_INSTANCE);
        WeldContainer container = weld.initialize();
        Lookup.init(container.select(CDILookupStrategy.class).get());
        LOGGER.info("Successfully initialized Weld/CDI and Lookup");
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        return weldInstance.select(clazz).get();
    }

    @Override
    public <U> U lookup(TypeLiteral<U> type) {
        return weldInstance.select(type).get();
    }

    @Override
    public <T> List<T> lookupAll(Class<T> clazz) {
        Instance<T> select = weldInstance.select(clazz);
        return lookupAll(select);
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
     * List<EventBus<ProgressEvent>> eventBusList = Lookup.lookupAll(new TypeLiteral<EventBus<ProgressEvent>>(){});
     * }</pre>
     *
     * @param type The type literal to search.
     * @return A list with all found service instances for the searched class.
     */
    @Override
    public <T> List<T> lookupAll(TypeLiteral<T> type) {
        Instance<T> select = weldInstance.select(type);
        return lookupAll(select);
    }

    /**
     * Concrete lookup all for a cdi {@link Instance} object.
     *
     * @param instance Use this instance to obtain the beans.
     * @param <T>      The type of all returned beans
     * @return A list with the found beans.
     */
    private <T> List<T> lookupAll(Instance<T> instance) {
        return StreamSupport.stream(instance.spliterator(), false)
                .sorted(Comparator.comparingInt((T t) -> ServiceLoaderLookupStrategy.getPriority(t.getClass())).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Shutdown the Weld CDI container.
     * This should be called when the application is shutting down.
     */
    public static void shutdownLookup() {
        if (weld != null) {
            try {
                LOGGER.info("Shutting down Weld/CDI container...");
                weld.shutdown();
                LOGGER.info("Weld/CDI container successfully shut down");
            } catch (IllegalStateException e) {
                LOGGER.warn("Weld container already shut down or not running: {}", e.getMessage());
            } catch (Exception e) {
                LOGGER.error("Error shutting down Weld/CDI container", e);
            } finally {
                weld = null;
            }
        }
    }

    static Weld getWeld() {
        return weld;
    }

    static void setWeld(Weld weld) {
        CDILookupStrategy.weld = weld;
    }
}
