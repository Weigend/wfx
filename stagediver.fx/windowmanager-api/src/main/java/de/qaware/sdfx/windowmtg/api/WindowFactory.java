package de.qaware.sdfx.windowmtg.api;

import javafx.stage.Stage;

/**
 * A window factory creates the stage of an new managed window. Managed windows are created when an user drops a view
 * outside the WindowManager manged area.
 * <p>
 * This is a {@link FunctionalInterface} whose functional method is {@link #initializeWindow()}.
 *
 * @author christian.fritz
 */
@FunctionalInterface
public interface WindowFactory {
    /**
     * Initialize a new stage that can be used for show managed windows.
     *
     * @return The new stage for a managed window.
     */
    Stage initializeWindow();
}
