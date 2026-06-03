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

import io.avaje.inject.BeanScope;
import io.softwareecg.wfx.platform.api.EventBus;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EventObject;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

/**
 * Verifies the Avaje-based {@link EventBusFactory} provides one shared
 * {@link SimpleEventBus} for typed and raw event bus lookups.
 */
public class EventBusFactoryTest {

    @Test
    public void parameterisedEventBusResolves() {
        try (BeanScope scope = BeanScope.builder().build()) {
            EventBus<?> raw = scope.get(EventBus.class);
            Type parameterised = new ParameterizedTypeReference<EventBus<EventObject>>() {
            }.getType();
            List<EventBus<EventObject>> beans = scope.list(parameterised);
            assertThat(beans, is(notNullValue()));
            assertThat(beans.size() >= 1, is(true));
            assertThat(beans.get(0), sameInstance(raw));
        }
    }

    @Test
    public void rawEventBusResolves() {
        try (BeanScope scope = BeanScope.builder().build()) {
            EventBus<?> bean = scope.get(EventBus.class);
            assertThat(bean, is(notNullValue()));
            assertThat(bean, instanceOf(SimpleEventBus.class));
        }
    }

    /**
     * Local type reference for one parameterised lookup.
     */
    private abstract static class ParameterizedTypeReference<T> {
        Type getType() {
            ParameterizedType pt = (ParameterizedType) getClass().getGenericSuperclass();
            return pt.getActualTypeArguments()[0];
        }
    }
}
