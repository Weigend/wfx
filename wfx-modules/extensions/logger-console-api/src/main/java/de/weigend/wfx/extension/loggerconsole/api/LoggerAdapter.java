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
package de.weigend.wfx.extension.loggerconsole.api;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * Adapter to retrieve the log messages from a logging framework like logback or log4j.
 *
 * @author christian.fritz
 */
public interface LoggerAdapter {

    /**
     * Get a list with all available log levels.
     *
     * @return The list of log levels.
     */
    List<String> getLevels();

    /**
     * Get the property to get the list with all available log levels.
     *
     * @return The levels property.
     */
    ReadOnlyListProperty<String> levelsProperty();

    /**
     * Get a list with all logger and the current active log levels.
     *
     * @return a list with all logger.
     */
    ObservableList<LoggerLevel> getLoggerLevels();

    /**
     * Get the list property for all logger
     *
     * @return the list property for all logger.
     */
    ListProperty<LoggerLevel> loggerLevelsProperty();

    /**
     * Get the property of log messages.
     *
     * @return the messages property.
     */
    ReadOnlyStringProperty messagesProperty();

    /**
     * Get the current log messages buffer.
     *
     * @return the current log messages within the buffer.
     */
    String getMessages();

    /**
     * Clear the messages buffer.
     * <p>
     * After executing this method {@link #getMessages()} returns an empty string.
     */
    void clearMessages();

    /**
     * A model class to show the loggers with its levels in ui and transfer them to the adapter.
     */
    final class LoggerLevel {
        private final ReadOnlyStringWrapper logger = new ReadOnlyStringWrapper(this, "logger");
        private final StringProperty level = new SimpleStringProperty(this, "level");

        /**
         * @param logger the loggers name
         * @param level  the current active level.
         */
        public LoggerLevel(String logger, String level) {
            this.logger.set(logger);
            this.level.set(level);
        }

        /**
         * @return the loggers name.
         */
        public String getLogger() {
            return logger.get();
        }

        /**
         * @return the loggers name property.
         */
        public ReadOnlyStringProperty loggerProperty() {
            return logger.getReadOnlyProperty();
        }

        /**
         * @return the current level.
         */
        public String getLevel() {
            return level.get();
        }

        /**
         * @return the level property
         */
        public StringProperty levelProperty() {
            return level;
        }

        /**
         * @param level the new logging level for this logger.
         */
        public void setLevel(String level) {
            this.level.set(level);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            LoggerLevel that = (LoggerLevel) o;

            return new EqualsBuilder()
                    .append(getLogger(), that.getLogger())
                    .append(getLevel(), that.getLevel())
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(getLogger())
                    .append(getLevel())
                    .toHashCode();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("logger", logger)
                    .append("level", level)
                    .toString();
        }
    }
}
