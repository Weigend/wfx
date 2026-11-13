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
package io.softwareecg.wfx.platform.core.fxml;

import io.avaje.inject.Bean;
import io.avaje.inject.BeanScope;
import io.avaje.inject.Factory;
import io.avaje.inject.Prototype;
import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.platform.core.eventbus.AnnotationProcessor;
import io.softwareecg.wfx.windowmanager.api.ViewLifecycle;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Produces a fresh {@link FXMLLoader} per lookup with a controller factory
 * that first resolves Avaje-managed controllers through {@link Lookup} and then
 * falls back to normal controller construction.
 */
@Factory
public class FXMLLoaderFactory {

    @Bean
    @Prototype
    public FXMLLoader createLoader() {
        return new PlatformFXMLLoader(new ArrayList<>());
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
     * Controllers whose {@code @EventSubscriber} methods get subscribed here
     * are recorded in {@code owned}, so the loader can end those subscriptions
     * when the loaded content is no longer shown. Application-wide
     * {@code @Singleton} controllers are left out: they outlive any single
     * view.
     */
    private static <T> T controllerFactory(Class<T> controllerClass, List<Object> owned) {
        T managed = Lookup.lookup(controllerClass);
        if (managed != null) {
            if (subscribe(managed) && !controllerClass.isAnnotationPresent(Singleton.class)) {
                owned.add(managed);
            }
            return managed;
        }
        try {
            T instance = controllerClass.getDeclaredConstructor().newInstance();
            BeanScope scope = Lookup.lookup(BeanScope.class);
            if (scope != null) {
                injectAnnotatedFields(instance, scope);
            }
            if (subscribe(instance)) {
                owned.add(instance);
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create FXML controller " + controllerClass, e);
        }
    }

    /**
     * Subscribe the controller's {@code @EventSubscriber} methods.
     *
     * @return {@code true} if this call created the subscriptions, i.e. the
     *         controller has such methods and was not subscribed before
     */
    private static boolean subscribe(Object controller) {
        if (AnnotationProcessor.isProcessed(controller)) {
            return false;
        }
        AnnotationProcessor.process(controller);
        return AnnotationProcessor.isProcessed(controller);
    }

    /**
     * The {@link FXMLLoader} handed out by the platform.
     * <p>
     * On top of the controller factory it ties the event-bus subscriptions of
     * the controllers it created — including those of nested
     * {@code <fx:include>} files, which share the factory — to the loaded root
     * node through {@link ViewLifecycle}. When the window manager unregisters
     * the view showing that node, the subscriptions end and the controllers
     * can be collected together with their scene graph.
     */
    static final class PlatformFXMLLoader extends FXMLLoader {

        private final List<Object> owned;

        PlatformFXMLLoader(List<Object> owned) {
            super(null, null, null, type -> controllerFactory(type, owned), StandardCharsets.UTF_8);
            this.owned = owned;
        }

        @Override
        public <T> T load() throws IOException {
            T root = super.load();
            bindLifecycle(root);
            return root;
        }

        @Override
        public <T> T load(InputStream inputStream) throws IOException {
            T root = super.load(inputStream);
            bindLifecycle(root);
            return root;
        }

        private void bindLifecycle(Object root) {
            if (owned.isEmpty() || !(root instanceof Node node)) {
                return;
            }
            List<Object> controllers = List.copyOf(owned);
            owned.clear();
            ViewLifecycle.bind(node, new ViewLifecycle.Hook() {
                @Override
                public void attached() {
                    controllers.forEach(AnnotationProcessor::process);
                }

                @Override
                public void detached() {
                    controllers.forEach(AnnotationProcessor::unprocess);
                }
            });
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
                    String qualifier = qualifierName(field);
                    if (!scope.contains(field.getType())) {
                        // No bean for this type at all; leave the legacy
                        // field-injection point untouched.
                        continue;
                    }
                    Object dep = (qualifier != null)
                            ? scope.get(field.getType(), qualifier)
                            : scope.get(field.getType());
                    field.set(instance, dep);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(
                            "Failed to inject field '" + field.getName() + "' on " + cls.getName(), e);
                }
            }
            cls = cls.getSuperclass();
        }
    }

    /**
     * Extract the Avaje qualifier name from a field's annotations. Avaje stores
     * a single name per bean (the simple name of the @Qualifier-meta annotation),
     * so an annotation like {@code @Embedded} on a field requests the bean named
     * "Embedded".
     */
    private static String qualifierName(Field field) {
        for (java.lang.annotation.Annotation a : field.getAnnotations()) {
            Class<? extends java.lang.annotation.Annotation> type = a.annotationType();
            if (type == Inject.class) {
                continue;
            }
            if (type.isAnnotationPresent(jakarta.inject.Qualifier.class)) {
                return type.getSimpleName();
            }
        }
        return null;
    }
}
