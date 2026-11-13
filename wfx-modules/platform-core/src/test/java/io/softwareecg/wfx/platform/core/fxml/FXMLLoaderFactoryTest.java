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

import io.avaje.inject.BeanScope;
import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.lookup.api.LookupStrategy;
import io.softwareecg.wfx.platform.api.EventBus;
import io.softwareecg.wfx.platform.api.EventSubscriber;
import io.softwareecg.wfx.platform.core.eventbus.AnnotationProcessor;
import io.softwareecg.wfx.platform.core.eventbus.SimpleEventBus;
import io.softwareecg.wfx.windowmanager.api.ViewLifecycle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @After
    public void tearDown() {
        Lookup.shutdown();
    }

    private static SimpleEventBus initLookupWithBus() {
        SimpleEventBus bus = new SimpleEventBus();
        LookupStrategy strategy = mock(LookupStrategy.class);
        when(strategy.lookup(EventBus.class)).thenReturn(bus);
        Lookup.init(strategy);
        return bus;
    }

    private static Node load(String fxml, List<Object> controllers) throws Exception {
        FXMLLoader loader = new FXMLLoaderFactory().createLoader();
        loader.setLocation(FXMLLoaderFactoryTest.class.getResource(fxml));
        Node root = loader.load();
        controllers.add(loader.getController());
        return root;
    }

    @Test
    public void controllerWithoutSubscriptionsIsNotRetained() throws Exception {
        initLookupWithBus();
        List<Object> controllers = new ArrayList<>();

        load("plain.fxml", controllers);

        // Regression: every controller used to end up in a static set of the
        // AnnotationProcessor and was never released.
        assertThat(AnnotationProcessor.isProcessed(controllers.get(0)), is(false));
    }

    @Test
    public void detachingTheViewEndsTheControllerSubscriptions() throws Exception {
        SimpleEventBus bus = initLookupWithBus();
        List<Object> controllers = new ArrayList<>();
        Node root = load("subscriber.fxml", controllers);
        SubscriberController controller = (SubscriberController) controllers.get(0);

        bus.publish(new EventObject(this));
        assertThat(controller.received, is(1));
        assertThat(NestedSubscriberController.lastCreated.received, is(1));

        ViewLifecycle.detached(root);
        bus.publish(new EventObject(this));

        assertThat("root controller must be unsubscribed", controller.received, is(1));
        assertThat("controllers of fx:include files must be unsubscribed too",
                NestedSubscriberController.lastCreated.received, is(1));
        assertThat(AnnotationProcessor.isProcessed(controller), is(false));
    }

    @Test
    public void reattachingTheViewSubscribesAgainExactlyOnce() throws Exception {
        SimpleEventBus bus = initLookupWithBus();
        List<Object> controllers = new ArrayList<>();
        Node root = load("subscriber.fxml", controllers);
        SubscriberController controller = (SubscriberController) controllers.get(0);

        ViewLifecycle.attached(root);   // registration of the fresh view: no second subscription
        bus.publish(new EventObject(this));
        assertThat(controller.received, is(1));

        ViewLifecycle.detached(root);
        ViewLifecycle.attached(root);   // the same view instance is registered again
        bus.publish(new EventObject(this));
        assertThat(controller.received, is(2));

        ViewLifecycle.detached(root);
    }

    public static class PlainController {
    }

    public static class SubscriberController {
        int received;

        @EventSubscriber(eventClass = EventObject.class)
        public boolean onEvent(EventObject event) {
            received++;
            return true;
        }
    }

    public static class NestedSubscriberController {
        static NestedSubscriberController lastCreated;
        int received;

        public NestedSubscriberController() {
            lastCreated = this;
        }

        @EventSubscriber(eventClass = EventObject.class)
        public boolean onEvent(EventObject event) {
            received++;
            return true;
        }
    }
}
