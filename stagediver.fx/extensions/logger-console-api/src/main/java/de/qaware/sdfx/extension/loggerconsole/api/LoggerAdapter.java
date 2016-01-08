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
package de.qaware.sdfx.extension.loggerconsole.api;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;

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
     * Get the current log level.
     *
     * @return the log level.
     */
    String getLevel();

    /**
     * Set the log level.
     *
     * @param level The new log level.
     */
    void setLevel(String level);

    /**
     * Get the log level property.
     *
     * @return The log level property.
     */
    StringProperty levelProperty();

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
}
