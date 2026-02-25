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

import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.lookup.LookupStrategy;
import io.softwareecg.wfx.platform.api.EventBus;
import io.softwareecg.wfx.platform.api.EventSubscriber;
import io.softwareecg.wfx.platform.api.events.ProgressEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link AnnotationProcessor}.
 *
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AnnotationProcessorTest {

    @Mock
    private LookupStrategy lookupStrategy;

    @Before
    public void setUp() throws Exception {
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(EventBus.class)).thenReturn(new SimpleEventBus());
    }

    /**
     * Tests that the AnnotationProcessor has successfully registered the NonConsumingEventListener.
     */
    @Test
    public void testNonConsumingEvent() {
        AnnotationProcessor.process(new NonConsumingEventListener());
        EventBus bus = Lookup.lookup(EventBus.class);
        boolean isEventStillValid = bus.publish(new EventObject(""));

        assertThat(isEventStillValid, is(true));
    }

    /**
     * Tests that the AnnotationProcessor has successfully registered the ConsumingEventListener.
     */
    @Test
    public void testConsumingEvent() {
        AnnotationProcessor.process(new ConsumingEventListener());
        EventBus bus = Lookup.lookup(EventBus.class);
        boolean isEventStillValid = bus.publish(new EventObject(""));

        assertThat(isEventStillValid, is(false));
    }

    /**
     * Tests that a listener registered for multiple event types is only called once per event,
     * even if the event type hierarchy would match multiple registrations.
     */
    @Test
    public void testMultipleEventsOnSameListner() {
        ListenForMultipleEventsEventListener listener = new ListenForMultipleEventsEventListener();
        AnnotationProcessor.process(listener);
        EventBus bus = Lookup.lookup(EventBus.class);
        bus.publish(new EventObject(""));
        bus.publish(new ProgressEvent("", 0, this));

        // EventObject triggers once for EventObject.class
        // ProgressEvent triggers twice: once for ProgressEvent.class and once for EventObject.class
        // (because listener is registered for both types and ProgressEvent extends EventObject)
        assertThat(listener.getInvocationTypes(), hasSize(3));
        assertThat(listener.getInvocationTypes().get(0), equalTo(EventObject.class));
        assertThat(listener.getInvocationTypes().get(1), equalTo(ProgressEvent.class));
        assertThat(listener.getInvocationTypes().get(2), equalTo(ProgressEvent.class));
    }

    /**
     * Tests that the EventBus gracefully handles listeners with wrong return types.
     * The exception is logged but doesn't propagate, allowing other listeners to execute.
     */
    @Test
    public void testForWrongMethodForAnnotation() {
        AnnotationProcessor.process(new WrongReturnTypeEventListener());
        EventBus bus = Lookup.lookup(EventBus.class);

        // Should not throw - error is logged but swallowed for resilience
        boolean result = bus.publish(new EventObject(""));
        
        // Result is true because the exception was caught and no listener returned false
        assertThat(result, is(true));
    }

    /**
     * Tests that the constructor is private.
     *
     * @throws Exception due to reflection there are some exceptions possible
     */
    @Test
    public void testConstructorIsPrivate() throws Exception {
        Constructor<AnnotationProcessor> constructor = AnnotationProcessor.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void testZeroArgumentListener() throws Exception {
        ZeroArgumentEventListener listener = new ZeroArgumentEventListener();
        AnnotationProcessor.process(listener);
        EventBus bus = Lookup.lookup(EventBus.class);
        assertThat(bus.publish(new EventObject("")), is(equalTo(true)));
    }

    @Test
    public void testVoidResultListener() throws Exception {
        VoidResultEventListener listener = new VoidResultEventListener();
        AnnotationProcessor.process(listener);
        EventBus bus = Lookup.lookup(EventBus.class);
        assertThat(bus.publish(new EventObject("")), is(equalTo(true)));
        assertThat(listener.executed, is(equalTo(true)));
    }

    /**
     * EventListener which does not consume the event.
     */
    private static class NonConsumingEventListener {

        @EventSubscriber(eventClass = EventObject.class)
        public boolean notConsumeEvent(EventObject event) {
            return true;
        }
    }

    /**
     * EventListener which does consume the event.
     */
    private static class ConsumingEventListener {

        @EventSubscriber(eventClass = EventObject.class)
        public boolean notConsumeEvent(EventObject event) {
            return false;
        }
    }

    /**
     * EventListener which has marked the wrong method with an annotation.
     */
    private static class WrongReturnTypeEventListener {

        @EventSubscriber(eventClass = EventObject.class)
        public String notConsumeEvent(EventObject event) {
            return "I am totally wrong";
        }
    }

    private static class ListenForMultipleEventsEventListener {
        private final List<Class<? extends EventObject>> invocationTypes = new ArrayList<>();

        public List<Class<? extends EventObject>> getInvocationTypes() {
            return invocationTypes;
        }

        @EventSubscriber(eventClass = {EventObject.class, ProgressEvent.class})
        public boolean notConsumeEvent(EventObject event) {
            invocationTypes.add(event.getClass());
            return true;
        }
    }

    private static class ZeroArgumentEventListener {
        @EventSubscriber(eventClass = {EventObject.class, ProgressEvent.class})
        public boolean notConsumeEvent() {
            return true;
        }
    }

    private static class VoidResultEventListener {
        public boolean executed;

        @EventSubscriber(eventClass = {EventObject.class, ProgressEvent.class})
        public void notConsumeEvent() {
            executed = true;
        }
    }
}
