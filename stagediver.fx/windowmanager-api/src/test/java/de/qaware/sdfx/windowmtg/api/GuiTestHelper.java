//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-api
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 24.03.15 17:18
//      description:
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.api;

import com.google.common.util.concurrent.SettableFuture;
import org.loadui.testfx.utils.FXTestUtils;

import javafx.application.*;
import javafx.stage.*;
import java.util.concurrent.TimeUnit;

/**
 * Helper methods to test the GUI.
 *
 * @author christian.fritz
 */
public class GuiTestHelper {
    private static final SettableFuture<Stage> stageFuture = SettableFuture.create();
    private static Stage stage;

    /**
     * Test JavaFX Application.
     */
    public static class TestFxApp extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.show();
            stageFuture.set(primaryStage);
        }
    }

    /**
     * Get the stage from the {@link de.qaware.sdfx.windowmtg.api.GuiTestHelper.TestFxApp} application. It will be
     * initialized if it is not.
     *
     * @return The stage.
     */
    public static Stage getStage() {
        if (stage == null) {
            FXTestUtils.launchApp(TestFxApp.class);
            try {
                stage = stageFuture.get(25, TimeUnit.SECONDS);
                FXTestUtils.bringToFront(stage);
            }
            catch (Exception e) {
                throw new RuntimeException("Unable to show stage", e);
            }
        }
        return stage;
    }
}
