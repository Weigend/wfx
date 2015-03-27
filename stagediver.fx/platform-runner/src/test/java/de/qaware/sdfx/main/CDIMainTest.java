package de.qaware.sdfx.main;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.cdi.CDILookupStrategy;
import de.qaware.sdfx.platform.api.PlatformApplication;
import org.junit.Test;

import javafx.stage.*;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for the cid specific startup in {@link de.qaware.sdfx.main.CDIMain}.
 *
 * @author christian.fritz
 */
public class CDIMainTest {

    @Test
    public void testInit() throws Exception {
        CDIMain cdiMain = new CDIMain();
        cdiMain.init();
        assertThat(Lookup.getLookupStrategy(), is(instanceOf(CDILookupStrategy.class)));
    }

    public static class App implements PlatformApplication {
        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getVersion() {
            return null;
        }

        @Override
        public void preload() {
        }

        @Override
        public void start() {
        }

        @Override
        public void showPreloader(Stage stage) {
        }

        @Override
        public void hidePreloader() {
        }

        @Override
        public void showMainApplicationWindow(Stage stage) {
        }

        @Override
        public void stop() {
        }
    }
}
