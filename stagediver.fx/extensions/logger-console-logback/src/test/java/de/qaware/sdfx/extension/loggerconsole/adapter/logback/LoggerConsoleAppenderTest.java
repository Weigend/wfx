package de.qaware.sdfx.extension.loggerconsole.adapter.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.EchoEncoder;
import ch.qos.logback.core.encoder.Encoder;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link LoggerConsoleAppender}.
 *
 * @author christian.fritz
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
        appender.doAppend(buildEvent("before5"));
        appender.doAppend(buildEvent("before6"));

        assertThat(lastMessages, is(equalTo("before2\r\nbefore3\r\nbefore4\r\nbefore5\r\nbefore6\r\n\n")));
    }

    private ILoggingEvent buildEvent(String msg) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.toString()).thenReturn(msg);
        return event;
    }
}