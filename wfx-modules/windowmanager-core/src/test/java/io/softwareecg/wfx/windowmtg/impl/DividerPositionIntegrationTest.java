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
package io.softwareecg.wfx.windowmtg.impl;

import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.lookup.serviceloader.ServiceLoaderLookupStrategy;
import io.softwareecg.wfx.windowmtg.testutil.GuiTestHelper;
import io.softwareecg.wfx.windowmtg.api.Position;
import io.softwareecg.wfx.windowmtg.api.View;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

/**
 * Integration test for divider position stability.
 * <p>
 * Wires a real WindowManagerImpl into a real Scene on a real Stage and asserts
 * that the SplitPane divider matches the requested viewAreaSize after layout.
 * Reproduces the long-standing "sometimes 50/50 instead of N/M" bug where
 * setDividerPositions() runs before the SplitPane has been sized by the FX
 * skin, so the explicit positions get clobbered by the skin's default
 * fair-distribution algorithm during the first layout pass.
 * <p>
 * The test does NOT use {@link io.softwareecg.wfx.windowmtg.api.JavaFXThreadingRule}
 * because we need to repeatedly yield control back to the FX thread between
 * registration / show() and the assertion so that the deferred re-apply
 * scheduled by {@code ViewStatus.applyDividerPosition()} via
 * {@link javafx.application.Platform#runLater(Runnable)} actually executes.
 * Each {@link GuiTestHelper#runInJavaFxThreadAndWait} call drains the FX queue
 * up to its own task, so chaining several of them pumps any pending pulse work.
 */
public class DividerPositionIntegrationTest {

    private static final double TOLERANCE = 0.02;

    @BeforeClass
    public static void initFx() throws Exception {
        // Boot the JavaFX runtime once for the test class.
        GuiTestHelper.getStage();
        Lookup.init(new ServiceLoaderLookupStrategy());
    }

    @Test
    public void leftSidePanelDividerMatchesRequestedSize() throws Exception {
        Fixture f = new Fixture();
        f.setUpSized();
        GuiTestHelper.runInJavaFxThreadAndWait(() -> {
            View center = new SizedTestView("center", Position.CENTER, 0.5);
            View explorer = new SizedTestView("explorer", Position.LEFT, 0.20);
            f.windowManager.register(center);
            f.windowManager.register(explorer, center);
        });
        f.pumpAndLayout();
        f.assertOuterDividerPositionCloseTo(0.20);
        f.tearDown();
    }

    @Test
    public void rightSidePanelDividerMatchesRequestedSize() throws Exception {
        Fixture f = new Fixture();
        f.setUpSized();
        GuiTestHelper.runInJavaFxThreadAndWait(() -> {
            View center = new SizedTestView("center", Position.CENTER, 0.5);
            View aichat = new SizedTestView("aichat", Position.RIGHT, 0.25);
            f.windowManager.register(center);
            f.windowManager.register(aichat, center);
        });
        f.pumpAndLayout();
        // RIGHT panel of size 0.25 → divider at 0.75
        f.assertOuterDividerPositionCloseTo(0.75);
        f.tearDown();
    }

    @Test
    public void multipleSidePanelsKeepIndependentRequestedSizes() throws Exception {
        Fixture f = new Fixture();
        f.setUpSized();
        GuiTestHelper.runInJavaFxThreadAndWait(() -> {
            View center = new SizedTestView("center", Position.CENTER, 0.5);
            View left = new SizedTestView("left", Position.LEFT, 0.15);
            View right = new SizedTestView("right", Position.RIGHT, 0.20);
            View bottom = new SizedTestView("bottom", Position.BOTTOM, 0.30);
            f.windowManager.register(center);
            f.windowManager.register(left, center);
            f.windowManager.register(right, center);
            f.windowManager.register(bottom, center);
        });
        f.pumpAndLayout();
        f.assertOuterDividerPositionCloseTo(0.15);
        f.tearDown();
    }

    @Test
    public void dividerSurvivesRegistrationBeforeStageIsSized() throws Exception {
        // Reproduces the realistic startup ordering: a preload-phase module
        // registers its views BEFORE the Stage has been laid out. JavaFX's
        // SplitPane skin recomputes divider positions during its first
        // layout pass — when register() runs against a width=0 SplitPane,
        // an unguarded setDividerPositions(...) call gets clobbered and the
        // skin falls back to fair-distribution. This test is the regression
        // guard for that scenario.
        Fixture f = new Fixture();
        f.setUpUnsized();
        GuiTestHelper.runInJavaFxThreadAndWait(() -> {
            View center = new SizedTestView("center", Position.CENTER, 0.5);
            View left = new SizedTestView("left", Position.LEFT, 0.20);
            f.windowManager.register(center);
            f.windowManager.register(left, center);
        });
        // Now the Stage gets sized & shown — divider must end up at 0.20
        // when the deferred re-apply has run.
        GuiTestHelper.runInJavaFxThreadAndWait(() -> {
            f.testStage.setWidth(800);
            f.testStage.setHeight(600);
            f.testStage.show();
        });
        f.pumpAndLayout();
        f.assertOuterDividerPositionCloseTo(0.20);
        f.tearDown();
    }

    private static final class Fixture {
        WindowManagerImpl windowManager;
        Stage testStage;
        BorderPane root;

        void setUpSized() throws Exception {
            GuiTestHelper.runInJavaFxThreadAndWait(() -> {
                windowManager = new WindowManagerImpl();
                root = new BorderPane();
                root.setCenter(windowManager.getRootPane());
                testStage = new Stage();
                testStage.setScene(new Scene(root, 800, 600));
                testStage.setWidth(800);
                testStage.setHeight(600);
                testStage.show();
                windowManager.init();
                root.applyCss();
                root.layout();
            });
        }

        void setUpUnsized() throws Exception {
            GuiTestHelper.runInJavaFxThreadAndWait(() -> {
                windowManager = new WindowManagerImpl();
                root = new BorderPane();
                root.setCenter(windowManager.getRootPane());
                testStage = new Stage();
                testStage.setScene(new Scene(root));
                // Note: NOT shown, NOT sized — Scene exists but root has 0x0.
                windowManager.init();
            });
        }

        void pumpAndLayout() throws Exception {
            // Drain runLater'd reapply tasks (queued by ViewStatus.applyDividerPosition).
            // Each runInJavaFxThreadAndWait queues a fence task and waits — by the
            // time it returns, all tasks queued before the fence have run.
            // Several pumps are needed because the reapply itself queues another
            // runLater after the listener-driven re-apply, and we want to land
            // AFTER the SplitPane skin's own layout pulse finishes redistributing.
            for (int i = 0; i < 5; i++) {
                GuiTestHelper.runInJavaFxThreadAndWait(() -> {
                    // Just yield — do NOT call layout() here, that would re-trigger
                    // the SplitPane skin's redistribution and clobber a freshly
                    // re-applied divider.
                });
            }
        }

        void assertOuterDividerPositionCloseTo(double expected) throws Exception {
            AtomicReference<Double> actual = new AtomicReference<>();
            GuiTestHelper.runInJavaFxThreadAndWait(() -> {
                SplitPane split = findSplitPane(windowManager.getRootPane());
                actual.set(split.getDividerPositions()[0]);
            });
            assertThat(actual.get(), closeTo(expected, TOLERANCE));
        }

        void tearDown() throws Exception {
            GuiTestHelper.runInJavaFxThreadAndWait(() -> {
                if (testStage != null) {
                    testStage.close();
                }
            });
        }
    }

    private static SplitPane findSplitPane(Parent root) {
        if (root instanceof SplitPane sp) {
            return sp;
        }
        for (var child : root.getChildrenUnmodifiable()) {
            if (child instanceof Parent p) {
                SplitPane found = findSplitPane(p);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Minimal {@link View} for layout tests with a configurable area size.
     */
    private static final class SizedTestView implements View {
        private final String id;
        private final Position position;
        private final double size;
        private final Label root;

        SizedTestView(String id, Position position, double size) {
            this.id = id;
            this.position = position;
            this.size = size;
            this.root = new Label(id);
        }

        @Override
        public String getViewId() {
            return id;
        }

        @Override
        public String getTitle() {
            return id;
        }

        @Override
        public String getToolTipInfo() {
            return id;
        }

        @Override
        public Position getDefaultPosition() {
            return position;
        }

        @Override
        public Parent getRootNode() {
            return root;
        }

        @Override
        public double getViewAreaSize() {
            return size;
        }
    }
}
