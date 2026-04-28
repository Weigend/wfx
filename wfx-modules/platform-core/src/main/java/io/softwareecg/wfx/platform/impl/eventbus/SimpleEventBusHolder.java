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
package io.softwareecg.wfx.platform.impl.eventbus;

/**
 * Single shared {@link SimpleEventBus} instance used by both the CDI
 * {@link EventBusProducer} and the Avaje {@link EventBusFactory}. Ensures
 * subscribers/publishers always meet on the same bus regardless of which DI
 * container is active during the migration.
 */
final class SimpleEventBusHolder {

    static final SimpleEventBus INSTANCE = new SimpleEventBus();

    private SimpleEventBusHolder() {
    }
}
