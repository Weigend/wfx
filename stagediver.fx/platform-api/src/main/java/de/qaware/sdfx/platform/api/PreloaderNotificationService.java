package de.qaware.sdfx.platform.api;

import org.osgi.framework.Bundle;

import javafx.application.*;

/**
 * This is the preloader notification service interface. The implementing service deliver the sended notifications to
 * the preloader splash screen. This service is only available during the startup phase.
 */
public interface PreloaderNotificationService {
    /**
     * Send a new notification to the preloader.
     *
     * @param bundle   The sending bundle.
     * @param message  The message to show.
     * @param progress The progress info value.
     * @return Indicates if the message was delivered directly to the preloader (true) or the message was stored for
     *         a later delivery (false).
     */
    boolean sendNotification(Bundle bundle, String message, double progress);

    /**
     * Send a default preloader notification to the splash screen.
     *
     * @param notification The notification.
     * @return Indicates if the message was delivered directly to the preloader (true) or the message was stored for
     *         a later delivery (false).
     */
    boolean sendNotification(Preloader.PreloaderNotification notification);
}
