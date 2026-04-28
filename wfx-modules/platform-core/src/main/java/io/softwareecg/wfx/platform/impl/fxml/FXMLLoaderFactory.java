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
package io.softwareecg.wfx.platform.impl.fxml;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import io.avaje.inject.Prototype;
import io.softwareecg.wfx.lookup.Lookup;
import javafx.fxml.FXMLLoader;

import java.nio.charset.StandardCharsets;

/**
 * Avaje Inject equivalent of the legacy CDI {@code FXMLLoaderProducer} from the
 * {@code cdi-contexts} module. Produces a new {@link FXMLLoader} per inject point
 * (via {@link Prototype}) with a controller factory that resolves controllers
 * through {@link Lookup}, so the active strategy (CDI or Avaje) decides.
 * <p>
 * Will replace {@code FXMLLoaderProducer} once the {@code cdi-contexts} module is
 * removed in S1.6.
 */
@Factory
public class FXMLLoaderFactory {

    @Bean
    @Prototype
    public FXMLLoader createLoader() {
        return new FXMLLoader(null, null, null, FXMLLoaderFactory::controllerFactory, StandardCharsets.UTF_8);
    }

    private static <T> T controllerFactory(Class<T> controllerClass) {
        return Lookup.lookup(controllerClass);
    }
}
