#set($symbol_pound='#')
#set($symbol_dollar='$')
#set($symbol_escape='\' )
package ${package}.application;

import com.google.inject.AbstractModule;
import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.guice.GuiceLookupStrategy;
import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.Module;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.platform.impl.PlatformApplicationImpl;
import de.qaware.sdfx.platform.impl.eventbus.SimpleEventBus;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import de.qaware.sdfx.windowmtg.impl.ViewContainerAreaFactory;
import de.qaware.sdfx.windowmtg.impl.ViewContainerAreaFactoryImpl;
import de.qaware.sdfx.windowmtg.impl.WindowManagerImpl;

/**
 * Demonstration of the stagediver.fx start with Guice as lookup mechanism.
 *
 * @author christian.fritz
 */
public class GuiceMain extends de.qaware.sdfx.main.Main {

    public static void main(String[] args) {
        Lookup.init(new GuiceLookupStrategy(new GuiceModule()));
        launch(args);
    }

    private static class GuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(de.qaware.sdfx.windowmtg.api.ApplicationWindow.class).to(ApplicationWindow.class).asEagerSingleton();
            bind(Module.class).to(testModule.class).asEagerSingleton();
            bind(PlatformApplication.class).to(PlatformApplicationImpl.class).asEagerSingleton();
            bind(EventBus.class).to(SimpleEventBus.class).asEagerSingleton();
            bind(WindowManager.class).to(WindowManagerImpl.class).asEagerSingleton();
            bind(ViewContainerAreaFactory.class).to(ViewContainerAreaFactoryImpl.class).asEagerSingleton();
        }
    }
}
