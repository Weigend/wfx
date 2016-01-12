package de.qaware.sdfx.extension.loggerconsole.adapter.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.LoggerContextListener;
import de.qaware.sdfx.extension.loggerconsole.api.LoggerAdapter;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static java.util.Collections.unmodifiableList;

/**
 * The logging adapter.
 *
 * @author christian.fritz
 */
public class LogbackAdapter implements LoggerAdapter, LoggerContextListener {
    public static final String DEFAULT = "Default";
    private final ReadOnlyListWrapper<String> levels =
            new ReadOnlyListWrapper<>(FXCollections.observableArrayList(DEFAULT, "OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL"));
    private final SimpleListProperty<LoggerLevel> loggerLevels =
            new SimpleListProperty<>(this, "loggerLevels", FXCollections.observableArrayList());
    private final ReadOnlyStringWrapper messages = new ReadOnlyStringWrapper(this, "messages");
    private final LoggerConsoleAppender appender = new LoggerConsoleAppender(messages::set);

    private Map<Logger, Level> previousLevel = new WeakHashMap<>();

    /**
     * Initialize a new logback logging console adapter.
     */
    public LogbackAdapter() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        LoggerContext context = rootLogger.getLoggerContext();
        context.addListener(this);
        appender.setContext(context);
        appender.setName("LoggingConsole");
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        appender.setEncoder(encoder);
        appender.start();

        context.getLoggerList().forEach(this::initLogger);
        rootLogger.addAppender(appender);
    }

    private void initLogger(Logger logger) {
        if (previousLevel.containsKey(logger)) {
            return;
        }
        LoggerLevel loggerLevel = new LoggerLevel(logger.getName(), DEFAULT);
        previousLevel.put(logger, logger.getLevel());
        loggerLevel.levelProperty().addListener((o, ov, nv) -> adjustLogger(logger, ov, nv));
        loggerLevels.add(loggerLevel);
    }

    private void adjustLogger(Logger logger, String ov, String nv) {
        if (StringUtils.equals(ov, DEFAULT)) {
            logger.setLevel(Level.toLevel(nv));
        }
        else if (StringUtils.equals(nv, DEFAULT)) {
            logger.setLevel(previousLevel.get(logger));
        }
        else {
            logger.setLevel(Level.toLevel(nv));
        }
    }

    @Override
    public List<String> getLevels() {
        return unmodifiableList(levels);
    }

    @Override
    public ReadOnlyListProperty<String> levelsProperty() {
        return levels.getReadOnlyProperty();
    }

    @Override
    public ObservableList<LoggerLevel> getLoggerLevels() {
        return loggerLevels.get();
    }

    @Override
    public ListProperty<LoggerLevel> loggerLevelsProperty() {
        return loggerLevels;
    }

    @Override
    public ReadOnlyStringProperty messagesProperty() {
        return messages.getReadOnlyProperty();
    }

    @Override
    public String getMessages() {
        return messages.get();
    }

    @Override
    public void clearMessages() {
        appender.clear();
        messages.set("");
    }

    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onStart(LoggerContext context) {
        context.getLoggerList().forEach(this::initLogger);
    }

    @Override
    public void onReset(LoggerContext context) {
        context.getLoggerList().forEach(this::initLogger);
    }

    @Override
    public void onStop(LoggerContext context) {
        context.getLoggerList().forEach(this::initLogger);
    }

    @Override
    public void onLevelChange(Logger logger, Level level) {
        initLogger(logger);
    }
}
