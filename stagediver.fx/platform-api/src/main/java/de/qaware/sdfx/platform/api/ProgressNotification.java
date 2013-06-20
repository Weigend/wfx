package de.qaware.sdfx.platform.api;

import javafx.application.*;

/**
 * Progress Notification message for the preloading splash screen.
 */
public class ProgressNotification extends Preloader.ProgressNotification {

    private final String message;

    /**
     * Create a new progress notification. Only with progress info but without any message.
     *
     * @param v The progress info.
     */
    public ProgressNotification(double v) {
        super(v);
        message = null;
    }

    /**
     * Create a new progress notification with progress info and a message.
     *
     * @param value   The progress value.
     * @param message The progress info message.
     */
    public ProgressNotification(double value, String message) {
        super(value);
        this.message = message;
    }

    /**
     * Get the progress info message.
     *
     * @return The message.
     */
    public String getMessage() {
        return message;
    }
}