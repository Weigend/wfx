/*
 * Created by IntelliJ IDEA.
 * User: christian.fritz
 * Date: 23.09.2014
 * Time: 17:06
 */
package de.qaware.sdfx.windowmtg;

import com.google.inject.AbstractModule;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import de.qaware.sdfx.windowmtg.impl.*;
import de.qaware.sdfx.windowmtg.windows.DefaultApplicationWindow;

public class WindowManagerModule extends AbstractModule {
    protected void configure() {
        bind(ApplicationWindow.class).to(DefaultApplicationWindow.class);
        bind(WindowManager.class).to(MultiWindowManager.class);
        bind(MultiWindowManager.class).to(WindowManagerImpl.class);
        bind(ViewConainterAreaFactory.class).to(ViewConainterAreaFactoryImpl.class);
        bind(DragNDropManager.class).to(DragNDropManagerImpl.class);
    }
}
