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

import io.avaje.inject.BeanScope;
import javafx.fxml.FXMLLoader;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

/**
 * Verifies the Avaje-based {@link FXMLLoaderFactory} produces a fresh
 * {@link FXMLLoader} per request.
 */
public class FXMLLoaderFactoryTest {

    @Test
    public void factoryProducesAFXMLLoader() {
        try (BeanScope scope = BeanScope.builder().build()) {
            FXMLLoader loader = scope.get(FXMLLoader.class);
            assertThat(loader, is(notNullValue()));
        }
    }

    @Test
    public void prototypeSemantics_eachLookupReturnsFreshInstance() {
        try (BeanScope scope = BeanScope.builder().build()) {
            FXMLLoader first = scope.get(FXMLLoader.class);
            FXMLLoader second = scope.get(FXMLLoader.class);
            assertThat(first, is(notNullValue()));
            assertThat(second, is(notNullValue()));
            assertThat("FXMLLoader must be a fresh instance per lookup",
                    second, is(not(sameInstance(first))));
        }
    }
}
