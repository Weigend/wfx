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
import io.softwareecg.wfx.platform.api.EventBusListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EventObject;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link io.softwareecg.wfx.platform.impl.eventbus.SimpleEventBus}
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SimpleEventBusTest {

    @Mock
    private LookupStrategy lookupStrategy;

    @Before
    public void setUp() throws Exception {
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(EventBus.class)).thenReturn(new SimpleEventBus());
    }

    @Test
    public void testGetBus() {
        EventBus bus = Lookup.lookup(EventBus.class);

        assertThat(bus, is(notNullValue()));
    }

    /**
     * Tests that the event is consumed from the second subscriber.
     */
    @Test
    public void testPublishWithTwoSubscribersOneConsumingEvent() {
        EventBus<EventObject> bus = Lookup.lookup(EventBus.class);

        // Register subscriber at the bus
        bus.subscribe(EventObject.class, event -> true);
        bus.subscribe(EventObject.class, event -> false);

        assertThat(bus.publish(new EventObject("message")), is(false));
    }

    /**
     * Tests that the message is still valid when there are no subscribers.
     */
    @Test
    public void testPublishWithNoSubscriber() {
        EventBus<EventObject> bus = Lookup.lookup(EventBus.class);
        assertThat(bus.publish(new EventObject("message")), is(true));
    }

    /**
     * Tests the publish with one not consuming subscriber.
     */
    @Test
    public void testPublishWithOneSubscriberNotConsumingEvent() {
        EventBus<EventObject> bus = Lookup.lookup(EventBus.class);
        // Register subscriber at the bus
        bus.subscribe(EventObject.class, event -> true);

        assertThat(bus.publish(new EventObject("message")), is(true));
    }

    /**
     * Tests the publish with one consuming subscriber.
     */
    @Test
    public void testPublishWithOneSubscriberConsumingEvent() {
        EventBus<EventObject> bus = Lookup.lookup(EventBus.class);
        // Register subscriber at the bus
        bus.subscribe(EventObject.class, event -> false);

        assertThat(bus.publish(new EventObject("message")), is(false));
    }

    @Test
    public void testUnSubscribe() throws Exception {
        EventBus<EventObject> bus = Lookup.lookup(EventBus.class);
        EventBusListener<EventObject> listener = event -> false;

        // Register subscriber at the bus

        bus.subscribe(EventObject.class, listener);
        assertThat(bus.publish(new EventObject("messsage")), is(false));
        bus.unsubscribe(EventObject.class, listener);
        assertThat(bus.publish(new EventObject("messsage")), is(true));
    }
}