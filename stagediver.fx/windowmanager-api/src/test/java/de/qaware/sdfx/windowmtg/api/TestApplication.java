// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 05.03.14 10:22
//      description: Test Application for unit tests
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.api;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaFX Test Application for unit tests.
 */
public class TestApplication extends Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestApplication.class);

    private static TestApplication instance;

    /**
     * Launch the application for unit tests.
     */
    public static void launchTest() {
        if (TestApplication.getInstance() == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Application.launch(TestApplication.class);
                }
            }).start();
        }
    }

    /**
     * Stop the test application after tests
     *
     * @throws Exception in case of the application can not be stopped.
     */
    public static void stopTest() throws Exception {
        TestApplication app;
        if ((app = TestApplication.getInstance()) != null) {
            app.stop();
        }
    }

    @Override
    public synchronized void start(Stage stage) throws Exception {
        LOGGER.info("Start Test-Application");
        instance = this;
    }

    @Override
    public synchronized void stop() throws Exception {
        LOGGER.info("Stop Test Application");
        instance = null;
    }

    private static synchronized TestApplication getInstance() {
        return instance;
    }
}
