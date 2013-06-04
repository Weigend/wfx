package de.qaware.sdfx.platform.api;

import org.osgi.framework.Bundle;

import javafx.application.*;

public interface PreloaderNotificationService {

    boolean sendNotification(Bundle bundle, String message, double progress);

    boolean sendNotification(Preloader.PreloaderNotification notification);
}
