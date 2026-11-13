/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2026 Weigend AM
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
package io.softwareecg.wfx.windowmanager.impl;

import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.lookup.serviceloader.ServiceLoaderLookupStrategy;
import io.softwareecg.wfx.windowmanager.api.Position;
import io.softwareecg.wfx.windowmanager.api.View;
import io.softwareecg.wfx.windowmanager.api.ViewKind;
import io.softwareecg.wfx.windowmanager.api.ViewLifecycle;
import io.softwareecg.wfx.windowmanager.impl.LayoutSnapshot.Area;
import io.softwareecg.wfx.windowmanager.impl.LayoutSnapshot.Split;
import io.softwareecg.wfx.windowmanager.impl.LayoutSnapshot.Tabs;
import io.softwareecg.wfx.windowmanager.impl.LayoutSnapshot.Window;
import io.softwareecg.wfx.windowmanager.testutil.GuiTestHelper;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration test for {@code WindowManager.saveLayout()} and
 * {@code restoreLayout()}: real window managers on real stages, as in an
 * application that stores its layout on exit and brings it back on the next
 * start.
 */
public class LayoutPersistenceIntegrationTest {

    private static final double TOLERANCE = 0.02;

    private final List<Workbench> workbenches = new ArrayList<>();

    @BeforeClass
    public static void initFx() {
        GuiTestHelper.getStage();
        Lookup.init(new ServiceLoaderLookupStrategy());
    }

    @After
    public void closeWindows() throws Exception {
        for (Workbench workbench : workbenches) {
            workbench.close();
        }
    }

    /**
     * One "application run": a window manager in a shown window.
     */
    private final class Workbench {
        private WindowManagerImpl windowManager;
        private Stage stage;

        Workbench() throws Exception {
            GuiTestHelper.runInJavaFxThreadAndWait(() -> {
                windowManager = new WindowManagerImpl();
                BorderPane root = new BorderPane();
                root.setCenter(windowManager.getRootPane());
                stage = new Stage();
                stage.setScene(new Scene(root, 800, 600));
                stage.show();
                windowManager.init();
            });
            workbenches.add(this);
        }

        /** The default registrations every run of the "application" makes. */
        Workbench withDefaultViews() throws Exception {
            onFx(() -> {
                View editor = new TestView("editor", Position.CENTER);
                windowManager.register(editor);
                windowManager.register(new TestView("explorer", Position.LEFT), editor);
                windowManager.register(new TestView("console", Position.BOTTOM), editor);
            });
            return this;
        }

        void onFx(GuiTestHelper.Runnable action) throws Exception {
            GuiTestHelper.runInJavaFxThreadAndWait(action);
            pump();
        }

        void pump() throws Exception {
            for (int i = 0; i < 5; i++) {
                GuiTestHelper.runInJavaFxThreadAndWait(() -> { });
            }
        }

        LayoutSnapshot snapshot() throws Exception {
            return LayoutSnapshot.parse(save());
        }

        String save() throws Exception {
            AtomicReference<String> text = new AtomicReference<>();
            onFx(() -> text.set(windowManager.saveLayout()));
            return text.get();
        }

        boolean restore(String layout) throws Exception {
            AtomicBoolean result = new AtomicBoolean();
            onFx(() -> result.set(windowManager.restoreLayout(layout)));
            return result.get();
        }

        List<String> visibleViewIds() {
            return windowManager.getVisibleViews().stream().map(View::getViewId).collect(Collectors.toList());
        }

        void close() throws Exception {
            GuiTestHelper.runInJavaFxThreadAndWait(() -> {
                for (RootArea subWindow : new ArrayList<>(windowManager.getRootAreas())) {
                    ((Stage) subWindow.getNode().getScene().getWindow()).close();
                }
                stage.close();
            });
        }
    }

    /** Compare two area trees, allowing the dividers the usual pixel rounding. */
    private static void assertSameArrangement(Area actual, Area expected) {
        if (expected instanceof Split expectedSplit) {
            assertThat(actual, instanceOf(Split.class));
            Split actualSplit = (Split) actual;
            assertThat(actualSplit.orientation(), is(expectedSplit.orientation()));
            assertThat(actualSplit.divider(), closeTo(expectedSplit.divider(), TOLERANCE));
            assertSameArrangement(actualSplit.first(), expectedSplit.first());
            assertSameArrangement(actualSplit.second(), expectedSplit.second());
        }
        else {
            assertThat(actual, is(equalTo(expected)));
        }
    }

    @Test
    public void layoutOfOneRunComesBackInTheNext() throws Exception {
        Workbench first = new Workbench().withDefaultViews();
        // The user rearranges: explorer narrower, console to the right.
        first.onFx(() -> {
            WindowManagerImpl wm = first.windowManager;
            View console = wm.findView("console");
            wm.unregister(console);
            wm.register(new TestView("console", Position.RIGHT), wm.findView("editor"));
        });
        LayoutSnapshot saved = first.snapshot();
        String text = first.save();

        Workbench second = new Workbench().withDefaultViews();
        assertThat(second.restore(text), is(true));

        LayoutSnapshot restored = second.snapshot();
        assertSameArrangement(restored.main(), saved.main());
        assertThat(restored.hidden(), is(equalTo(saved.hidden())));
    }

    @Test
    public void dividerMovedByTheUserIsRestored() throws Exception {
        Workbench first = new Workbench().withDefaultViews();
        Split outer = (Split) first.snapshot().main();
        String text = new LayoutSnapshot(
                new Split(outer.orientation(), 0.31, outer.first(), outer.second()),
                List.of(), List.of(), null).write();

        Workbench second = new Workbench().withDefaultViews();
        second.restore(text);

        assertThat(((Split) second.snapshot().main()).divider(), closeTo(0.31, TOLERANCE));
    }

    @Test
    public void restoredDividerSurvivesALaterRegistration() throws Exception {
        Workbench first = new Workbench().withDefaultViews();
        Split outer = (Split) first.snapshot().main();
        String text = new LayoutSnapshot(
                new Split(outer.orientation(), 0.31, outer.first(), outer.second()),
                List.of(), List.of(), null).write();
        Workbench second = new Workbench().withDefaultViews();
        second.restore(text);

        // Opening another view re-applies the default sizes of views that
        // still claim a side position; restored views must not be among them.
        second.onFx(() -> second.windowManager.register(new TestView("editor-2", Position.CENTER)));

        assertThat(((Split) second.snapshot().main()).divider(), closeTo(0.31, TOLERANCE));
    }

    @Test
    public void tabOrderAndSelectionAreRestored() throws Exception {
        String text = new LayoutSnapshot(
                new Tabs(true, List.of("b", "c", "a"), "c"), List.of(), List.of(), "c").write();
        Workbench workbench = new Workbench();
        workbench.onFx(() -> {
            workbench.windowManager.register(new TestView("a", Position.CENTER));
            workbench.windowManager.register(new TestView("b", Position.CENTER));
            workbench.windowManager.register(new TestView("c", Position.CENTER));
        });

        workbench.restore(text);

        Tabs tabs = (Tabs) workbench.snapshot().main();
        assertThat(tabs.views(), contains("b", "c", "a"));
        assertThat(tabs.selected(), is("c"));
        assertThat(workbench.windowManager.getFocusedView().getViewId(), is("c"));
    }

    @Test
    public void hiddenToolStaysHiddenButRegistered() throws Exception {
        Workbench first = new Workbench().withDefaultViews();
        first.onFx(() -> first.windowManager.closeView(first.windowManager.findView("console")));
        String text = first.save();

        Workbench second = new Workbench().withDefaultViews();
        second.restore(text);

        assertThat(second.windowManager.findView("console"), is(notNullValue()));
        assertThat(second.snapshot().hidden(), contains("console"));
        // ... and the user can still bring it back
        second.onFx(() -> second.windowManager.showView(second.windowManager.findView("console")));
        assertThat(second.snapshot().hidden(), is(empty()));
    }

    @Test
    public void viewThatNoLongerExistsIsSkippedAndItsPaneDropped() throws Exception {
        String text = new Workbench().withDefaultViews().save();

        Workbench second = new Workbench();
        second.onFx(() -> {
            View editor = new TestView("editor", Position.CENTER);
            second.windowManager.register(editor);
            second.windowManager.register(new TestView("console", Position.BOTTOM), editor);
            // no explorer in this run
        });
        second.restore(text);

        Area main = second.snapshot().main();
        assertThat("only the editor/console split is left", main, instanceOf(Split.class));
        assertThat(((Split) main).orientation(), is(Orientation.VERTICAL));
        assertThat(((Split) main).first(), instanceOf(Tabs.class));
        assertThat(((Split) main).second(), instanceOf(Tabs.class));
    }

    @Test
    public void viewTheLayoutDoesNotKnowKeepsBeingShown() throws Exception {
        String text = new Workbench().withDefaultViews().save();

        Workbench second = new Workbench().withDefaultViews();
        second.onFx(() -> second.windowManager.register(
                new TestView("new-tool", Position.RIGHT), second.windowManager.findView("editor")));
        second.restore(text);

        assertThat(second.snapshot().hidden(), is(empty()));
        assertThat(second.visibleViewIds().contains("new-tool"), is(true));
    }

    @Test
    public void centerViewsCanStillBeRegisteredWhenNothingOfTheLayoutIsLeft() throws Exception {
        String text = new Workbench().withDefaultViews().save();

        Workbench second = new Workbench();     // registers none of the saved views
        second.restore(text);
        second.onFx(() -> second.windowManager.register(new TestView("late", Position.CENTER)));

        assertThat(second.visibleViewIds(), contains("late"));
    }

    @Test
    public void detachedWindowIsReopenedWithItsBounds() throws Exception {
        String text = new LayoutSnapshot(
                new Tabs(true, List.of("editor"), "editor"),
                List.of(new Window(120, 140, 420, 330, false, new Tabs(true, List.of("chart"), "chart"))),
                List.of(), null).write();
        Workbench workbench = new Workbench();
        workbench.onFx(() -> {
            workbench.windowManager.register(new TestView("editor", Position.CENTER));
            workbench.windowManager.register(new TestView("chart", Position.CENTER));
        });

        workbench.restore(text);

        assertThat(workbench.windowManager.getRootAreas(), hasSize(1));
        Stage detached = (Stage) workbench.windowManager.getRootAreas().get(0).getNode().getScene().getWindow();
        assertThat(detached.isShowing(), is(true));
        assertThat(detached.getWidth(), closeTo(420, 2));
        assertThat(detached.getHeight(), closeTo(330, 2));
        assertThat(detached.getTitle(), is("chart"));

        LayoutSnapshot again = workbench.snapshot();
        assertThat(again.windows(), hasSize(1));
        assertThat(((Tabs) again.windows().get(0).root()).views(), contains("chart"));
        assertThat(((Tabs) again.main()).views(), contains("editor"));
    }

    @Test
    public void detachedWindowWithoutAnyOfItsViewsIsNotOpened() throws Exception {
        String text = new LayoutSnapshot(
                new Tabs(true, List.of("editor"), "editor"),
                List.of(new Window(120, 140, 420, 330, false, new Tabs(true, List.of("gone"), "gone"))),
                List.of(), null).write();
        Workbench workbench = new Workbench();
        workbench.onFx(() -> workbench.windowManager.register(new TestView("editor", Position.CENTER)));

        workbench.restore(text);

        assertThat(workbench.windowManager.getRootAreas(), is(empty()));
    }

    @Test
    public void restoringClosesWindowsTheLayoutDoesNotHave() throws Exception {
        String withWindow = new LayoutSnapshot(
                new Tabs(true, List.of("editor"), "editor"),
                List.of(new Window(120, 140, 420, 330, false, new Tabs(true, List.of("chart"), "chart"))),
                List.of(), null).write();
        String withoutWindow = new LayoutSnapshot(
                new Tabs(true, List.of("editor", "chart"), "editor"), List.of(), List.of(), null).write();
        Workbench workbench = new Workbench();
        workbench.onFx(() -> {
            workbench.windowManager.register(new TestView("editor", Position.CENTER));
            workbench.windowManager.register(new TestView("chart", Position.CENTER));
        });
        workbench.restore(withWindow);
        Stage detached = (Stage) workbench.windowManager.getRootAreas().get(0).getNode().getScene().getWindow();

        workbench.restore(withoutWindow);

        assertThat(detached.isShowing(), is(false));
        assertThat(workbench.windowManager.getRootAreas(), is(empty()));
        assertThat(((Tabs) workbench.snapshot().main()).views(), contains("editor", "chart"));
    }

    @Test
    public void unreadableLayoutLeavesTheArrangementAlone() throws Exception {
        Workbench workbench = new Workbench().withDefaultViews();
        String before = workbench.save();

        assertThat(workbench.restore("this is not a layout"), is(false));

        assertThat(workbench.save(), is(equalTo(before)));
    }

    @Test
    public void documentsStayRegisteredAndAttachedThroughARestore() throws Exception {
        Workbench workbench = new Workbench().withDefaultViews();
        View document = new TestView("doc", Position.CENTER) {
            @Override
            public ViewKind getKind() {
                return ViewKind.DOCUMENT;
            }
        };
        AtomicBoolean detached = new AtomicBoolean();
        ViewLifecycle.bind(document.getRootNode(), new ViewLifecycle.Hook() {
            @Override
            public void attached() {
            }

            @Override
            public void detached() {
                detached.set(true);
            }
        });
        workbench.onFx(() -> workbench.windowManager.register(document));
        String text = workbench.save();

        workbench.restore(text);

        assertThat(workbench.windowManager.findView("doc"), is(notNullValue()));
        assertThat("rearranging is not closing", detached.get(), is(false));
    }

    @Test
    public void viewsCanBeMovedAndClosedAfterARestore() throws Exception {
        // The restored areas must be wired like the ones built by register():
        // closing the last view of a pane has to collapse the split around it.
        String text = new Workbench().withDefaultViews().save();
        Workbench second = new Workbench().withDefaultViews();
        second.restore(text);

        second.onFx(() -> second.windowManager.closeView(second.windowManager.findView("explorer")));
        second.onFx(() -> second.windowManager.closeView(second.windowManager.findView("console")));

        assertThat(second.snapshot().main(), instanceOf(Tabs.class));
        assertThat(((Tabs) second.snapshot().main()).views(), contains("editor"));
    }
}
