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

/**
 * CDI Producer for EventBus.
 * <p>
 * This producer is needed because CDI cannot automatically resolve generic interfaces
 * like {@code EventBus<EventObject>} when looking up by raw type {@code EventBus.class}.
 *
 */
@ApplicationScoped
public class EventBusProducer {

    private static final SimpleEventBus INSTANCE = new SimpleEventBus();

    /**
     * Produces the singleton EventBus instance for CDI injection.
     *
     * @return the EventBus singleton
     */
    @Produces
    @Singleton
    @Default
    @SuppressWarnings("rawtypes")
    public EventBus produceEventBus() {
        return INSTANCE;
    }
}
