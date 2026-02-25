/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 Weigend AM
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
package io.softwareecg.wfx.extension.loggerconsole.adapter.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.encoder.Encoder;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link LoggerConsoleAppender}.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LoggerConsoleAppenderTest {

    private LoggerConsoleAppender appender;
    private String lastMessages;
    private Encoder<ILoggingEvent> encoder = new EchoEncoder<>();

    @Before
    public void setUp() throws Exception {
        appender = new LoggerConsoleAppender(s -> lastMessages = s, 5);
        appender.setEncoder(encoder);
        appender.start();
    }

    @Test
    public void testAppendClear() throws Exception {
        appender.doAppend(buildEvent("beforeClear"));
        assertThat(lastMessages, startsWith("beforeClear"));
        appender.clear();
        assertThat(lastMessages, Matchers.isEmptyString());
    }

    @Test
    public void testAppendShiftOut() throws Exception {
        appender.doAppend(buildEvent("before1"));
        appender.doAppend(buildEvent("before2"));
        appender.doAppend(buildEvent("before3"));
        appender.doAppend(buildEvent("before4"));
        Thread.sleep(1000);
        appender.doAppend(buildEvent("before5"));
        appender.doAppend(buildEvent("before6"));

        assertThat(lastMessages, startsWith("before2"));
    }

    private ILoggingEvent buildEvent(String msg) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.toString()).thenReturn(msg);
        return event;
    }
}