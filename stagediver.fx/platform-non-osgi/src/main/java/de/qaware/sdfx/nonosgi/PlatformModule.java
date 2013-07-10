package de.qaware.sdfx.nonosgi;

import com.google.inject.AbstractModule;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import de.qaware.sdfx.platform.impl.PreloaderNotificationServiceImpl;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import de.qaware.sdfx.windowmtg.impl.WindowManagerImpl;
import de.qaware.sdfx.windowmtg.windows.DefaultApplicationWindow;

public class PlatformModule extends AbstractModule {
    protected void configure() {
        bind(WindowManager.class).to(WindowManagerImpl.class).asEagerSingleton();
        bind(PreloaderNotificationService.class).to(PreloaderNotificationServiceImpl.class).asEagerSingleton();
        bind(ApplicationWindow.class).to(DefaultApplicationWindow.class).asEagerSingleton();
    }
}
