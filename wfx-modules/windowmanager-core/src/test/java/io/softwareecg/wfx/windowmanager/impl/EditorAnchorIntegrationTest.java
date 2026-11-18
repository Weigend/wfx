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
import io.softwareecg.wfx.windowmanager.api.ViewAnchor;
import io.softwareecg.wfx.windowmanager.api.ViewKind;
import io.softwareecg.wfx.windowmanager.impl.LayoutSnapshot.Area;
import io.softwareecg.wfx.windowmanager.impl.LayoutSnapshot.Split;
import io.softwareecg.wfx.windowmanager.impl.LayoutSnapshot.Tabs;
import io.softwareecg.wfx.windowmanager.testutil.GuiTestHelper;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

/**
 * A panel that belongs beside the editor pane ({@link ViewAnchor#EDITOR}):
 * where it ends up when it is registered, when documents come and go, and
 * when a layout is put back. Real panes in a real window.
 * <p>
 * The arrangement is written the way one would sketch it: {@code [a,b]} is a
 * tab pane with its views, {@code *[..]} the editor pane, {@code (x | y)} two
 * areas side by side and {@code (x / y)} one above the other.
 */
public class EditorAnchorIntegrationTest {

    private static final double TOLERANCE = 0.02;

    private final List<Stage> stages = new ArrayList<>();
    private WindowManagerImpl windowManager;

    @BeforeClass
    public static void initFx() {
        GuiTestHelper.getStage();
        Lookup.init(new ServiceLoaderLookupStrategy());
    }

    @Before
    public void openWindow() throws Exception {
        windowManager = newWorkbench();
    }

    @After
    public void closeWindows() throws Exception {
        GuiTestHelper.runInJavaFxThreadAndWait(() -> stages.forEach(Stage::close));
    }

    /** One "application run": a window manager in a shown window. */
    private WindowManagerImpl newWorkbench() throws Exception {
        AtomicReference<WindowManagerImpl> created = new AtomicReference<>();
        GuiTestHelper.runInJavaFxThreadAndWait(() -> {
            WindowManagerImpl manager = new WindowManagerImpl();
            BorderPane root = new BorderPane();
            root.setCenter(manager.getRootPane());
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
            manager.init();
            stages.add(stage);
            created.set(manager);
        });
        return created.get();
    }

    /** Run on the JavaFX thread and let the layout settle afterwards. */
    private static void onFx(GuiTestHelper.Runnable action) throws Exception {
        GuiTestHelper.runInJavaFxThreadAndWait(action);
        for (int i = 0; i < 5; i++) {
            GuiTestHelper.runInJavaFxThreadAndWait(() -> { });
        }
    }

    /** A tool at the side of the window, like an explorer. */
    private static View tool(String id, Position position) {
        return new TestView(id, position);
    }

    /** A tool beside the editor pane, taking the given share of it. */
    private static View editorPanel(String id, Position position, double size) {
        return new TestView(id, position) {
            @Override
            public ViewAnchor getDefaultAnchor() {
                return ViewAnchor.EDITOR;
            }

            @Override
            public double getViewAreaSize() {
                return size;
            }
        };
    }

    private static View editorPanel(String id, Position position) {
        return editorPanel(id, position, 0.5);
    }

    private static View document(String id) {
        return new TestView(id, Position.CENTER) {
            @Override
            public ViewKind getKind() {
                return ViewKind.DOCUMENT;
            }
        };
    }

    private static LayoutSnapshot layoutOf(WindowManagerImpl manager) throws Exception {
        AtomicReference<String> text = new AtomicReference<>();
        onFx(() -> text.set(manager.saveLayout()));
        return LayoutSnapshot.parse(text.get());
    }

    private String arrangement() throws Exception {
        return sketch(layoutOf(windowManager).main());
    }

    private static String sketch(Area area) {
        if (area instanceof Tabs tabs) {
            return (tabs.editor() ? "*" : "") + "[" + String.join(",", tabs.views()) + "]";
        }
        Split split = (Split) area;
        String between = split.orientation() == Orientation.HORIZONTAL ? " | " : " / ";
        return "(" + sketch(split.first()) + between + sketch(split.second()) + ")";
    }

    /** The divider of the split that has the editor pane as one of its sides. */
    private static double dividerAtEditor(Area area) {
        Split split = (Split) area;
        for (Area side : List.of(split.first(), split.second())) {
            if (side instanceof Tabs tabs && tabs.editor()) {
                return split.divider();
            }
        }
        return dividerAtEditor(split.first() instanceof Split ? split.first() : split.second());
    }

    /* ----- registering ------------------------------------------------------ */

    @Test
    public void thePanelSitsUnderTheEditorPaneBeforeAnyDocumentIsOpen() throws Exception {
        onFx(() -> {
            windowManager.register(tool("explorer", Position.LEFT));
            windowManager.register(editorPanel("console", Position.BOTTOM));
        });

        assertThat(arrangement(), is("([explorer] | (*[] / [console]))"));
    }

    @Test
    public void theOrderOfRegistrationMakesNoDifference() throws Exception {
        onFx(() -> {
            windowManager.register(editorPanel("console", Position.BOTTOM));
            windowManager.register(tool("explorer", Position.LEFT));
        });

        assertThat(arrangement(), is("([explorer] | (*[] / [console]))"));
    }

    /** The contrast: what {@link ViewAnchor#WINDOW}, the default, does. */
    @Test
    public void aPanelAtTheSideOfTheWindowSpansWhatIsThere() throws Exception {
        onFx(() -> {
            windowManager.register(tool("explorer", Position.LEFT));
            windowManager.register(tool("console", Position.BOTTOM));
        });

        assertThat(arrangement(), is("(([explorer] | *[]) / [console])"));
    }

    @Test
    public void thePanelTakesTheShareOfTheEditorPaneItAsksFor() throws Exception {
        onFx(() -> {
            windowManager.register(tool("explorer", Position.LEFT));
            windowManager.register(editorPanel("console", Position.BOTTOM, 0.25));
        });

        assertThat(dividerAtEditor(layoutOf(windowManager).main()), is(closeTo(0.75, TOLERANCE)));
    }

    @Test
    public void everySideOfTheEditorPaneCanTakeAPanel() throws Exception {
        onFx(() -> {
            windowManager.register(editorPanel("console", Position.BOTTOM));
            windowManager.register(editorPanel("properties", Position.RIGHT));
        });

        // Each panel takes its share from the editor pane as it is then, so
        // the one registered later is the one closer to the documents.
        assertThat(arrangement(), is("((*[] | [properties]) / [console])"));
    }

    @Test
    public void aParentViewStillDecidesWhereThePanelGoes() throws Exception {
        View explorer = tool("explorer", Position.LEFT);
        onFx(() -> {
            windowManager.register(explorer);
            windowManager.register(editorPanel("console", Position.BOTTOM), explorer);
        });

        assertThat(arrangement(), is("(([explorer] / [console]) | *[])"));
    }

    /* ----- documents come and go --------------------------------------------- */

    @Test
    public void documentsOpenAboveThePanelAndLeaveItInPlaceWhenTheyClose() throws Exception {
        View report = document("report");
        onFx(() -> {
            windowManager.register(tool("explorer", Position.LEFT));
            windowManager.register(editorPanel("console", Position.BOTTOM));
            windowManager.register(report);
            windowManager.register(document("chart"));
        });
        assertThat(arrangement(), is("([explorer] | (*[report,chart] / [console]))"));

        onFx(() -> {
            windowManager.closeView(report);
            windowManager.closeView(windowManager.findView("chart"));
        });

        assertThat(arrangement(), is("([explorer] | (*[] / [console]))"));
    }

    @Test
    public void aClosedPanelComesBackUnderTheEditorPane() throws Exception {
        View console = editorPanel("console", Position.BOTTOM);
        onFx(() -> {
            windowManager.register(tool("explorer", Position.LEFT));
            windowManager.register(console);
            windowManager.register(document("report"));
            windowManager.closeView(console);
        });
        assertThat(arrangement(), is("([explorer] | *[report])"));

        onFx(() -> windowManager.showView(console));

        assertThat(arrangement(), is("([explorer] | (*[report] / [console]))"));
    }

    @Test
    public void aPanelRegisteredHiddenIsPlacedWhenItIsShown() throws Exception {
        View console = editorPanel("console", Position.BOTTOM);
        onFx(() -> {
            windowManager.register(tool("explorer", Position.LEFT));
            windowManager.register(console, false);
        });
        assertThat(arrangement(), is("([explorer] | *[])"));

        onFx(() -> windowManager.showView(console));

        assertThat(arrangement(), is("([explorer] | (*[] / [console]))"));
    }

    /* ----- putting a layout back ---------------------------------------------- */

    @Test
    public void theDefaultLayoutPutsThePanelBackUnderTheEditorPane() throws Exception {
        onFx(() -> {
            windowManager.register(tool("explorer", Position.LEFT));
            windowManager.register(editorPanel("console", Position.BOTTOM));
            windowManager.register(document("report"));
        });
        // The user drags the console under the explorer.
        String rearranged = new LayoutSnapshot(
                new Split(Orientation.HORIZONTAL, 0.3,
                        new Split(Orientation.VERTICAL, 0.5,
                                new Tabs(false, List.of("explorer"), "explorer"),
                                new Tabs(false, List.of("console"), "console")),
                        new Tabs(true, List.of("report"), "report")),
                List.of(), List.of(), null).write();
        onFx(() -> windowManager.restoreLayout(rearranged));
        assertThat(arrangement(), is("(([explorer] / [console]) | *[report])"));

        onFx(() -> windowManager.restoreDefaultLayout());

        assertThat(arrangement(), is("([explorer] | (*[] / [console]))"));
    }

    @Test
    public void aLayoutOfAnEarlierRunPlacesThePanelBeforeAnyDocumentIsOpen() throws Exception {
        onFx(() -> {
            windowManager.register(tool("explorer", Position.LEFT));
            windowManager.register(editorPanel("console", Position.BOTTOM));
            windowManager.register(document("report"));
        });
        // The user makes the console smaller; the application is closed.
        Split outer = (Split) layoutOf(windowManager).main();
        Split atEditor = (Split) outer.second();
        String saved = new LayoutSnapshot(
                new Split(outer.orientation(), outer.divider(), outer.first(),
                        new Split(atEditor.orientation(), 0.8, atEditor.first(), atEditor.second())),
                List.of(), List.of(), null).write();

        // The next run: the tools are registered at startup, no document yet.
        WindowManagerImpl nextRun = newWorkbench();
        onFx(() -> {
            nextRun.register(tool("explorer", Position.LEFT));
            nextRun.register(editorPanel("console", Position.BOTTOM));
            nextRun.restoreLayout(saved);
        });

        LayoutSnapshot restored = layoutOf(nextRun);
        assertThat(sketch(restored.main()), is("([explorer] | (*[] / [console]))"));
        assertThat(dividerAtEditor(restored.main()), is(closeTo(0.8, TOLERANCE)));

        // And the first document of that run opens above the console.
        onFx(() -> nextRun.register(document("report")));
        LayoutSnapshot withDocument = layoutOf(nextRun);
        assertThat(sketch(withDocument.main()), is("([explorer] | (*[report] / [console]))"));
        assertThat(dividerAtEditor(withDocument.main()), is(closeTo(0.8, TOLERANCE)));
    }

    @Test
    public void aPanelTheLayoutDoesNotKnowGoesBesideTheEditorPane() throws Exception {
        onFx(() -> windowManager.register(tool("explorer", Position.LEFT)));
        AtomicReference<String> saved = new AtomicReference<>();
        onFx(() -> saved.set(windowManager.saveLayout()));

        // A newer version of the application adds a console.
        WindowManagerImpl nextRun = newWorkbench();
        onFx(() -> {
            nextRun.register(tool("explorer", Position.LEFT));
            nextRun.register(editorPanel("console", Position.BOTTOM));
            nextRun.restoreLayout(saved.get());
        });

        assertThat(sketch(layoutOf(nextRun).main()), is("([explorer] | (*[] / [console]))"));
    }
}
