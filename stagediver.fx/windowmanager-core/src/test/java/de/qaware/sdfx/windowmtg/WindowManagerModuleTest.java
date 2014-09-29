package de.qaware.sdfx.windowmtg;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

public class WindowManagerModuleTest {


    @Test
    public void testInitWindowManager() throws Exception {

        Injector injector = Guice.createInjector(new WindowManagerModule());

        WindowManager windowManager = injector.getInstance(WindowManager.class);
        assertThat(windowManager, is(notNullValue()));
        windowManager.init();
    }
}