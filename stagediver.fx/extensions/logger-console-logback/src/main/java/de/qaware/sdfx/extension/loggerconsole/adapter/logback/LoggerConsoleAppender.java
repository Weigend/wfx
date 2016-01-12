package de.qaware.sdfx.extension.loggerconsole.adapter.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.status.ErrorStatus;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
            messagesReceiver.accept(events.stream().collect(Collectors.joining()) + "\n");
        }
    }
}
