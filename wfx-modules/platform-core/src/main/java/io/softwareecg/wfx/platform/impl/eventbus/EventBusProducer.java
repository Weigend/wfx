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
package io.softwareecg.wfx.platform.impl.eventbus;

import io.softwareecg.wfx.platform.api.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.util.EventObject;

/**
 * CDI Producer for EventBus.
 * <p>
 * Produces the singleton {@link SimpleEventBus} instance for CDI injection.
 * Two methods are exposed:
 * <ul>
 *   <li>{@link #produceEventBus()} returns the parameterised
 *       {@code EventBus<EventObject>} type that satisfies typed inject points
 *       like {@code @Inject EventBus<EventObject> eventBus}.</li>
 *   <li>{@link #produceRawEventBus()} returns the raw {@code EventBus} type
 *       that {@code Lookup.lookup(EventBus.class)} resolves against.</li>
 * </ul>
 * Both methods return the same instance, so subscribers and publishers always
 * meet on the same bus regardless of how they look it up.
 * <p>
 * {@link SimpleEventBus} is annotated {@code @Vetoed} so CDI does not also
 * pick it up as a managed bean — that would create two candidates for
 * {@code EventBus.class} and lead to non-deterministic resolution.
 */
@ApplicationScoped
public class EventBusProducer {

    @Produces
    @Singleton
    @Default
    public EventBus<EventObject> produceEventBus() {
        return SimpleEventBusHolder.INSTANCE;
    }

    @Produces
    @Singleton
    @Default
    @SuppressWarnings("rawtypes")
    public EventBus produceRawEventBus() {
        return SimpleEventBusHolder.INSTANCE;
    }
}
