package de.qaware.sdfx.platform.api;

import java.io.Serializable;

/**
 * Preloader notification keys. They are used to group the notifications while preloading and startup the platform.
 *
 * @author christian.fritz
 */
public interface NotificationKey extends Serializable {

    /**
     * Get the notification key.
     *
     * @return the notification key.
     */
    String getNotificationKey();
}
