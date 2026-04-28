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
import io.avaje.inject.BeanScope;
import io.avaje.inject.Factory;
import io.avaje.inject.Prototype;
import io.softwareecg.wfx.lookup.Lookup;
import jakarta.inject.Inject;
import javafx.fxml.FXMLLoader;

import java.lang.reflect.Field;
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

    /**
     * Two-tier resolution:
     * <ol>
     *   <li>Ask the active {@link Lookup} strategy — for Avaje-managed FXML
     *       controllers ({@code @Singleton}/{@code @Prototype} with
     *       constructor-injected dependencies) this returns a fully wired
     *       instance.</li>
     *   <li>Fall back to {@code newInstance()} + reflection-based field
     *       injection. Required for legacy FXML controllers that still use
     *       {@code @Inject private Foo bar;} field-injection (these are not
     *       Avaje-discoverable, but we want them to keep working until they
     *       are migrated to constructor-injection one by one).</li>
     * </ol>
     */
    private static <T> T controllerFactory(Class<T> controllerClass) {
        T managed = Lookup.lookup(controllerClass);
        if (managed != null) {
            return managed;
        }
        try {
            T instance = controllerClass.getDeclaredConstructor().newInstance();
            BeanScope scope = Lookup.lookup(BeanScope.class);
            if (scope != null) {
                injectAnnotatedFields(instance, scope);
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create FXML controller " + controllerClass, e);
        }
    }

    private static void injectAnnotatedFields(Object instance, BeanScope scope) {
        Class<?> cls = instance.getClass();
        while (cls != null && cls != Object.class) {
            for (Field field : cls.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Inject.class)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    Object dep = scope.get(field.getType());
                    field.set(instance, dep);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(
                            "Failed to inject field '" + field.getName() + "' on " + cls.getName(), e);
                }
            }
            cls = cls.getSuperclass();
        }
    }
}
