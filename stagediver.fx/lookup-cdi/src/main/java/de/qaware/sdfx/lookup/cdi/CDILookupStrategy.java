/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
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
package de.qaware.sdfx.lookup.cdi;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Using Contexts and Dependency Injection (CDI) as lookup strategy.
 *
 * @author christian.fritz
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
                .map(t -> Pair.of(t, ServiceLoaderLookupStrategy.getPriority(t.getClass())))
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .map(Pair::getKey)
                .collect(Collectors.toList());
    }

    static Weld getWeld() {
        return weld;
    }

    static void setWeld(Weld weld) {
        CDILookupStrategy.weld = weld;
    }
}
