// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 17.07.13 08:30
//     description: Simple logger for the startup of stagediver.fx
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.main;

import java.io.PrintStream;

/**
 * This is a simple logger for the startup of stagediver.fx.
 */
public class StartupLogger {

    private static PrintStream writer = System.err;
    private String name;

    /**
     * Initialize a new logger.
     *
     * @param clazz The class where this logger should be used.
     */
    public StartupLogger(Class clazz) {
        name = clazz.getName();
    }

    /**
     * Log a simple message on error
     *
     * @param message The message to log.
     */
    public void error(String message) {
        writeMessage(Level.ERROR, message);
    }

    /**
     * Log a exception on error.
     *
     * @param message The message to log.
     * @param e       The exception to log.
     */
    public void error(String message, Throwable e) {
        writeMessage(Level.ERROR, message, e);
    }

    /**
     * Log a formatted message on error.
     *
     * @param message The format string. See {@link String#format(String, Object...)} for the format string.
     * @param args    The objects to include into the message.
     */
    public void error(String message, Object... args) {
        writeMessage(Level.ERROR, message, args);
    }

    /**
     * Log a simple message on warning
     *
     * @param message The message to log.
     */
    public void warn(String message) {
        writeMessage(Level.WARN, message);
    }

    /**
     * Log a exception on warning.
     *
     * @param message The message to log.
     * @param e       The exception to log.
     */
    public void warn(String message, Throwable e) {
        writeMessage(Level.WARN, message, e);
    }

    /**
     * Log a formatted message on warning.
     *
     * @param message The format string. See {@link String#format(String, Object...)} for the format string.
     * @param args    The objects to include into the message.
     */
    public void warn(String message, Object... args) {
        writeMessage(Level.WARN, message, args);
    }

    /**
     * Log a simple message on info
     *
     * @param message The message to log.
     */
    public void info(String message) {
        writeMessage(Level.INFO, message);
    }

    /**
     * Log a exception on info.
     *
     * @param message The message to log.
     * @param e       The exception to log.
     */
    public void info(String message, Throwable e) {
        writeMessage(Level.INFO, message, e);
    }

    /**
     * Log a formatted message on info.
     *
     * @param message The format string. See {@link String#format(String, Object...)} for the format string.
     * @param args    The objects to include into the message.
     */
    public void info(String message, Object... args) {
        writeMessage(Level.INFO, message, args);
    }

    /**
     * Log a simple message on debug
     *
     * @param message The message to log.
     */
    public void debug(String message) {
        writeMessage(Level.DEBUG, message);
    }

    /**
     * Log a exception on debug.
     *
     * @param message The message to log.
     * @param e       The exception to log.
     */
    public void debug(String message, Throwable e) {
        writeMessage(Level.DEBUG, message, e);
    }

    /**
     * Log a formatted message on debug.
     *
     * @param message The format string. See {@link String#format(String, Object...)} for the format string.
     * @param args    The objects to include into the message.
     */
    public void debug(String message, Object... args) {
        writeMessage(Level.DEBUG, message, args);
    }

    private void writeMessage(Level level, String message) {
        if (!level.isEnabled()) {
            return;
        }
        writeMessage(level, message, new String[]{""});
    }

    private void writeMessage(Level level, String message, Throwable e) {
        if (!level.isEnabled()) {
            return;
        }
        String msg = String.format("[%s]%s %s - %s - %s%n", Thread.currentThread().getName(), level, name, message,
                e.getMessage());
        writer.print(msg);
        e.printStackTrace(writer);
        writer.print('\n');
    }

    private void writeMessage(Level level, String message, Object[] args) {
        if (!level.isEnabled()) {
            return;
        }
        String msg = String.format(message, args);
        msg = String.format("[%s] %s %s - %s%n", Thread.currentThread().getName(), level, name, msg);
        writer.print(msg);
    }

    /**
     * Available log levels
     */
    public enum Level {
        ERROR,
        WARN,
        INFO,
        DEBUG(false);
        private boolean enabled;

        private Level() {
            this(true);
        }

        private Level(boolean enabled) {
            this.enabled = Boolean.parseBoolean(System.getProperty("sdfx.logger." + this.name(), Boolean.toString(enabled)));
        }

        /**
         * Is this log level enabled?
         *
         * @return If this log level is enabled.
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Enable or disable this log level.
         *
         * @param enabled The new status of the log level.
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
