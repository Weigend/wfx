/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 Weigend AM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.weigend.wfx.windowmtg.api;

import com.google.common.util.concurrent.SettableFuture;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.loadui.testfx.utils.FXTestUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Helper methods to test the GUI.
 *
 * @author Software-EKG Team
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
     * Get the stage from the {@link de.weigend.wfx.windowmtg.api.GuiTestHelper.TestFxApp} application. It will be
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

    /**
     * Executes the argument in the JavaFX Application thread and wait until the execution is finished.
     *
     * @param runnable The executed action.
     * @throws Exception In case the execution of the argument causes an exception.
     */
    public static void runInJavaFxThreadAndWait(Runnable runnable) throws Exception {
        new JfxExecutor(runnable).execute();
    }

    /**
     * Functional interface to execute something within the application thread.
     */
    @FunctionalInterface
    public interface Runnable {
        void run() throws Exception;
    }

    /**
     * Actual implementation of {@link GuiTestHelper#runInJavaFxThreadAndWait(Runnable)}.
     */
    private static class JfxExecutor {

        private final Runnable runnable;
        private Exception rethrownException;

        public JfxExecutor(Runnable runnable) {
            this.runnable = runnable;
        }

        private void execute() throws Exception {
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    runnable.run();
                }
                catch (Exception e) {
                    rethrownException = e;
                }
                finally {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            if (rethrownException != null) {
                throw rethrownException;
            }
        }
    }
}
