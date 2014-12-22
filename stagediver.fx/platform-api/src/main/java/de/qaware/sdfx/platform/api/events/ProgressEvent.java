package de.qaware.sdfx.platform.api.events;

import java.util.EventObject;

/**
 * This event is to report the startup progress to the splash screen and to the global progress bar.
 *
 * @author christian.fritz
 */
public class ProgressEvent extends EventObject {

    /**
     * The event message. It will be shown to the user.
     */
    private String message;

    /**
     * The progress.
     * The value must be between 0 and 1.
     */
    private double progress;

    /**
     * ProgressEvent for calculating a progress.
     *
     * @param message  the message the Event will carry
     * @param progress the amount of progress. it must be between 0 and 1.
     * @param source   the object creating this event
     */
    public ProgressEvent(String message, double progress, Object source) {
        super(source);
        this.message = message;
        this.progress = progress;
    }

    /**
     * Returns the Event's message.
     *
     * @return the Event's message or an empty String.
     */
    public String getMessage() {
        return message != null ? message : "";
    }

    /**
     * Returns the Event's progress.
     *
     * @return the Event's progress.
     */
    public double getProgress() {
        return progress;
    }
}
