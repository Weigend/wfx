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