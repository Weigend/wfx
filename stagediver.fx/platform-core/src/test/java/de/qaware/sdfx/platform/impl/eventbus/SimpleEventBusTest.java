package de.qaware.sdfx.platform.impl.eventbus;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.platform.api.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.EventObject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link de.qaware.sdfx.platform.impl.eventbus.SimpleEventBus}
 *
 * @author christian.fritz
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

        assertNotNull(bus);
    }

    /**
     * Tests that the event is consumed from the second subscriber.
     */
    @Test
    public void testPublishWithTwoSubscribersOneConsumingEvent() {
        EventBus bus = Lookup.lookup(EventBus.class);

        // Register subscriber at the bus
        bus.subscribe(EventObject.class, event -> true);
        bus.subscribe(EventObject.class, event -> false);

        boolean isEventStillValid = bus.publish(new EventObject("message"));

        assertFalse(isEventStillValid);
    }

    /**
     * Tests that the message is still valid when there are no subscribers.
     */
    @Test
    public void testPublishWithNoSubscriber() {
        EventBus bus = Lookup.lookup(EventBus.class);
        boolean isEventStillValid = bus.publish(new EventObject("message"));

        assertTrue(isEventStillValid);
    }

    /**
     * Tests the publish with one not consuming subscriber.
     */
    @Test
    public void testPublishWithOneSubscriberNotConsumingEvent() {
        EventBus bus = Lookup.lookup(EventBus.class);
        // Register subscriber at the bus
        bus.subscribe(EventObject.class, event -> true);

        boolean isEventStillValid = bus.publish(new EventObject("message"));

        assertTrue(isEventStillValid);
    }

    /**
     * Tests the publish with one consuming subscriber.
     */
    @Test
    public void testPublishWithOneSubscriberConsumingEvent() {
        EventBus bus = Lookup.lookup(EventBus.class);
        // Register subscriber at the bus
        bus.subscribe(EventObject.class, event -> false);

        boolean isEventStillValid = bus.publish(new EventObject("message"));

        assertFalse(isEventStillValid);
    }
}