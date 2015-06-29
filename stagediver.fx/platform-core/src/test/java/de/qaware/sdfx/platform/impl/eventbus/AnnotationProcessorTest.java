/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
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
package de.qaware.sdfx.platform.impl.eventbus;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.EventSubscriber;
import de.qaware.sdfx.platform.api.events.ProgressEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
 * @author christian.fritz
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

    @Test
    public void testMultipleEventsOnSameListner() {
        ListenForMultipleEventsEventListener listener = new ListenForMultipleEventsEventListener();
        AnnotationProcessor.process(listener);
        EventBus bus = Lookup.lookup(EventBus.class);
        bus.publish(new EventObject(""));
        bus.publish(new ProgressEvent("", 0, this));

        assertThat(listener.getInvocationTypes(), contains(EventObject.class, ProgressEvent.class));
    }

    /**
     * Tests that the AnnotationProcessor has registered a wrong object.
     */
    @Test(expected = IllegalStateException.class)
    public void testForWrongMethodForAnnotation() {
        AnnotationProcessor.process(new WrongReturnTypeEventListener());
        EventBus bus = Lookup.lookup(EventBus.class);

        // Throws a IllegalStateException
        bus.publish(new EventObject(""));
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
        private List<Class<? extends EventObject>> invocationTypes = new ArrayList<>();

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
