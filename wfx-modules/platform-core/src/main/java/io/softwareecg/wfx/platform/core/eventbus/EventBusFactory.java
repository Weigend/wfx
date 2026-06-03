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
package io.softwareecg.wfx.platform.core.eventbus;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.softwareecg.wfx.platform.api.EventBus;

import java.util.EventObject;

/**
 * Avaje Inject event bus provider.
 * <p>
 * Both bean methods return the same {@link SimpleEventBus} instance so raw
 * {@code EventBus} lookups and typed {@code EventBus<EventObject>} injections
 * publish and subscribe on one shared bus.
 * <ul>
 *   <li>{@link #produceEventBus()} for parameterised
 *       {@code EventBus<EventObject>} inject points.</li>
 *   <li>{@link #produceRawEventBus()} for {@code Lookup.lookup(EventBus.class)}
 *       calls that operate on the raw type.</li>
 * </ul>
 */
@Factory
public class EventBusFactory {

    private final SimpleEventBus eventBus = new SimpleEventBus();

    @Bean
    public EventBus<EventObject> produceEventBus() {
        return eventBus;
    }

    @Bean
    @SuppressWarnings("rawtypes")
    public EventBus produceRawEventBus() {
        return eventBus;
    }
}
