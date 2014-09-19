package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.platform.api.NotificationKey;

/**
 * Platform notification keys
 *
 * @author christian.fritz
 */
public enum PlatformNotificationKeys implements NotificationKey {
    /**
     * Core Notifications
     */
    CORE,
    /**
     * Window Management Notifications
     */
    WINDOW_MANAGEMENT;

    @Override
    public String getNotificationKey() {
        return name();
    }
}
