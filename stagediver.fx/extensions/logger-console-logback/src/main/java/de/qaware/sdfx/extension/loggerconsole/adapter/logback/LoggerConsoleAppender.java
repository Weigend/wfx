/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 QAware GmbH
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
package de.qaware.sdfx.extension.loggerconsole.adapter.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.status.ErrorStatus;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Special log appender that creates a string of the last x log events.
 *
 * @author christian.fritz
 */
public class LoggerConsoleAppender extends OutputStreamAppender<ILoggingEvent> {
    public static final int BUFFER_SIZE = 64 * 1024;
    private final Collection<String> events;
    private final Consumer<String> messagesReceiver;
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(BUFFER_SIZE);
    private final RateLimiter limiter = RateLimiter.create(4);

    /**
     * Init a new log appender with the given messages receiver and a capacity of 500 events.
     *
     * @param messagesReceiver the messages receiver.
     */
    public LoggerConsoleAppender(Consumer<String> messagesReceiver) {
        this(messagesReceiver, 500);
    }

    /**
     * init a new messages receiver.
     *
     * @param messagesReceiver the messages receiver.
     * @param capacity         the maximum events to hold for scrollback.
     */
    public LoggerConsoleAppender(Consumer<String> messagesReceiver, int capacity) {
        this.messagesReceiver = messagesReceiver;
        this.events = new CircularFifoQueue<>(capacity);
    }

    /**
     * Remove all hold events.
     */
    public void clear() {
        events.clear();
        messagesReceiver.accept("");
    }

    @Override
    public void start() {
        getEncoder().setContext(context);
        setOutputStream(outputStream);
        encoder.start();
        super.start();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted()) {
            return;
        }
        lock.lock();
        try {
            super.append(eventObject);
            outputStream.flush();
            events.add(outputStream.toString("UTF-8"));
        }
        catch (IOException e) {
            addStatus(new ErrorStatus("IO failure in appender", this, e));
        }
        finally {
            outputStream.reset();
            if (limiter.tryAcquire()) {
                String messages = StringUtils.join(events.toArray());
                lock.unlock();
                messagesReceiver.accept(messages);
            }
            else {
                lock.unlock();
            }
        }
    }
}
