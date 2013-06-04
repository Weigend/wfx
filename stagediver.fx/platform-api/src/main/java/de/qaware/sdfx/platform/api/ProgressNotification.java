package de.qaware.sdfx.platform.api;

import javafx.application.*;

public class ProgressNotification extends Preloader.ProgressNotification {

    private final String message;

    public ProgressNotification(double v) {
        super(v);
        message = null;
    }

    public ProgressNotification(double value, String message) {
        super(value);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}