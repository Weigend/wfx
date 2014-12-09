// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 21.06.13 09:55
//     description:
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.platform.api.NotificationKey;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import de.qaware.sdfx.platform.api.ProgressNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the preloader notification service.
 */
public class PreloaderNotificationServiceImpl implements PreloaderNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreloaderNotificationServiceImpl.class);
    private PlatformApplication application;
    private Map<NotificationKey, Double> values = new HashMap<>();
    private List<Preloader.PreloaderNotification> notifications = new ArrayList<>();

    protected PlatformApplication getApplication() {
        return application;
    }

    protected void setApplication(PlatformApplication application) {
        this.application = application;
        sendQueuedNotifications();
    }

    private void sendQueuedNotifications() {
        for (Preloader.PreloaderNotification notification : notifications) {
            application.notifyPreloader(notification);
        }
        notifications = null;
    }

    @Override
    public boolean sendNotification(NotificationKey key, String message, double progress) {
        values.put(key, progress);
        double sum = 0;
        for (Double val : values.values()) {
            sum += val;
        }
        return sendNotification(new ProgressNotification(sum / values.size(), message));
    }

    @Override
    public boolean sendNotification(Preloader.PreloaderNotification notification) {

        if (application == null) {
            notifications.add(notification);
            LOGGER.debug("Queued notification {}", notification);
            return false;
        }
        application.notifyPreloader(notification);
        return true;
    }
}
