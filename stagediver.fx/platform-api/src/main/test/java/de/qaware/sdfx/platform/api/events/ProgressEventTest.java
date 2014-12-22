package de.qaware.sdfx.platform.api.events;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;

/**
 * Unit test for the {@link de.qaware.sdfx.platform.api.events.ProgressEvent}
 *
 * @author christian.fritz
 */
public class ProgressEventTest {
    /**
     * Tests for null as source.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullAsSource() {
        new ProgressEvent("Message", 0, null);
    }

    /**
     * Tests for the message.
     */
    @Test
    public void testGetMessage() {
        ProgressEvent event = new ProgressEvent("Message", 0, this);
        String message = event.getMessage();
        assertThat(message, is(equalTo("Message")));
    }

    /**
     * Tests for null as message.
     */
    @Test
    public void testGetNullMessage() {
        ProgressEvent event = new ProgressEvent(null, 0, this);
        String message = event.getMessage();
        assertThat(message, is(equalTo("")));
    }

    /**
     * Tests for the progress.
     */
    @Test
    public void testGetProgress() {
        ProgressEvent event = new ProgressEvent(null, 42.0, this);
        double progress = event.getProgress();
        assertThat(progress, is(closeTo(42.0, 0.0)));
    }
}