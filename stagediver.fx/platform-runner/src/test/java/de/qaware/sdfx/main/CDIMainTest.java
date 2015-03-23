package de.qaware.sdfx.main;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.cdi.CDILookupStrategy;
import de.qaware.sdfx.platform.api.PlatformApplication;
import de.qaware.sdfx.platform.api.exceptions.PlatformException;
import javafx.stage.Stage;
import org.junit.Test;

import java.io.IOException;

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
        public void showPreloader(Stage stage) throws IOException {

        }

        @Override
        public void hidePreloader() {

        }

        @Override
        public void showMainApplicationWindow(Stage stage) throws PlatformException, IOException {

        }

        @Override
        public void stop() {

        }
    }
}