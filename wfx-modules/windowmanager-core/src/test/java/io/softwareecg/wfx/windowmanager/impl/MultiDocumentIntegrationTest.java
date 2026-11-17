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
import io.softwareecg.wfx.lookup.api.LookupContext;
import io.softwareecg.wfx.lookup.api.MutableLookupContext;
import io.softwareecg.wfx.lookup.serviceloader.ServiceLoaderLookupStrategy;
import io.softwareecg.wfx.windowmanager.api.Position;
import io.softwareecg.wfx.windowmanager.api.View;
import io.softwareecg.wfx.windowmanager.api.ViewKind;
import io.softwareecg.wfx.windowmanager.testutil.GuiTestHelper;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * An application that shows several documents, each in one or more views:
 * which view is in front, and what {@code WindowManager.getActiveLookup()}
 * answers when views come and go. Real tab panes in a real window, because
 * both depend on which tabs the user can see.
 * <p>
 * Showing the window moves the keyboard focus into a tab pane at a time the
 * test does not control, and the focus decides what is active. Every test
 * therefore sets the focus it starts from itself and then acts and reads the
 * result within one run on the JavaFX thread.
 */
public class MultiDocumentIntegrationTest {

    /** Stands for the thing several views can show: a file, a model, an analysis. */
    private record Document(String name) {
    }

    private WindowManagerImpl windowManager;
    private Stage stage;

    @BeforeClass
    public static void initFx() {
        GuiTestHelper.getStage();
        Lookup.init(new ServiceLoaderLookupStrategy());
    }

    @Before
    public void openWindow() throws Exception {
        GuiTestHelper.runInJavaFxThreadAndWait(() -> {
            windowManager = new WindowManagerImpl();
            BorderPane root = new BorderPane();
            root.setCenter(windowManager.getRootPane());
            stage = new Stage();
            stage.setScene(new Scene(root, 800, 600));
            stage.show();
            windowManager.init();
        });
    }

    @After
    public void closeWindow() throws Exception {
        GuiTestHelper.runInJavaFxThreadAndWait(() -> stage.close());
    }

    /** Run on the JavaFX thread and let layout and focus settle afterwards. */
    private void onFx(GuiTestHelper.Runnable action) throws Exception {
        GuiTestHelper.runInJavaFxThreadAndWait(action);
        for (int i = 0; i < 5; i++) {
            GuiTestHelper.runInJavaFxThreadAndWait(() -> { });
        }
    }

    /** A document view: closing it unregisters it. */
    private static View documentView(String id, Position position, Object... content) {
        return view(id, position, ViewKind.DOCUMENT, new MutableLookupContext(content));
    }

    /** A tool that does not take part in the active context, like a console. */
    private static View silentTool(String id, Position position) {
        return view(id, position, ViewKind.TOOL, LookupContext.EMPTY);
    }

    private static View view(String id, Position position, ViewKind kind, LookupContext lookup) {
        return new TestView(id, position) {
            @Override
            public ViewKind getKind() {
                return kind;
            }

            @Override
            public LookupContext getLookup() {
                return lookup;
            }
        };
    }

    private Document activeDocument() {
        return windowManager.getActiveLookup().lookup(Document.class);
    }

    private List<String> visibleViewIds() {
        List<String> ids = new ArrayList<>();
        windowManager.getVisibleViews().forEach(view -> ids.add(view.getViewId()));
        return ids;
    }

    /* ----- opening ---------------------------------------------------------- */

    @Test
    public void aSecondDocumentInTheSamePaneComesToTheFront() throws Exception {
        Document first = new Document("first");
        Document second = new Document("second");
        AtomicReference<List<String>> visible = new AtomicReference<>();
        AtomicReference<Document> active = new AtomicReference<>();

        onFx(() -> {
            windowManager.register(documentView("first", Position.CENTER, first));
            windowManager.register(documentView("second", Position.CENTER, second));
            visible.set(visibleViewIds());
            active.set(activeDocument());
        });

        assertThat("what is active is what the user sees", visible.get(), contains("second"));
        assertThat(active.get(), is(second));
    }

    @Test
    public void aToolJoiningAPaneStaysBehindTheSelectedTab() throws Exception {
        AtomicReference<List<String>> visible = new AtomicReference<>();

        onFx(() -> {
            windowManager.register(silentTool("explorer", Position.CENTER));
            windowManager.register(silentTool("outline", Position.CENTER));
            visible.set(visibleViewIds());
        });

        assertThat(visible.get(), contains("explorer"));
    }

    @Test
    public void aDocumentRegisteredHiddenStaysHidden() throws Exception {
        View shown = documentView("shown", Position.CENTER, new Document("shown"));
        View hidden = documentView("hidden", Position.CENTER, new Document("hidden"));
        AtomicReference<List<String>> visible = new AtomicReference<>();

        onFx(() -> {
            windowManager.register(shown);
            windowManager.register(hidden, false);
            visible.set(visibleViewIds());
        });

        assertThat(visible.get(), contains("shown"));
    }

    /* ----- closing ---------------------------------------------------------- */

    @Test
    public void closingOneOfTwoDocumentsSideBySideLeavesTheOtherActive() throws Exception {
        Document left = new Document("left");
        Document right = new Document("right");
        View leftView = documentView("left", Position.CENTER, left);
        View rightView = documentView("right", Position.RIGHT, right);
        onFx(() -> {
            windowManager.register(leftView);
            // Not a tab next to the first one: a pane of its own beside it.
            windowManager.register(rightView, leftView);
        });
        AtomicReference<Document> before = new AtomicReference<>();
        AtomicReference<Document> after = new AtomicReference<>();

        onFx(() -> {
            windowManager.setFocusedView(leftView);
            windowManager.setFocusedView(rightView);
            before.set(activeDocument());
            windowManager.closeView(rightView);
            after.set(activeDocument());
        });

        assertThat(before.get(), is(right));
        assertThat("the document still on screen takes over", after.get(), is(left));
    }

    @Test
    public void closingTheLastDocumentEmptiesTheActiveContext() throws Exception {
        View only = documentView("only", Position.CENTER, new Document("only"));
        onFx(() -> windowManager.register(only));
        AtomicReference<Document> after = new AtomicReference<>(new Document("not asked yet"));

        onFx(() -> {
            windowManager.setFocusedView(only);
            windowManager.closeView(only);
            after.set(activeDocument());
        });

        assertThat(after.get(), is(nullValue()));
    }

    @Test
    public void aViewBehindAnotherTabDoesNotTakeOver() throws Exception {
        Document behind = new Document("behind another tab");
        Document closing = new Document("closing");
        View behindView = documentView("behind", Position.CENTER, behind);
        View console = silentTool("console", Position.CENTER);
        View closingView = documentView("closing", Position.RIGHT, closing);
        onFx(() -> {
            windowManager.register(behindView);
            // Same pane, and without a context of its own.
            windowManager.register(console);
            windowManager.register(closingView, behindView);
        });
        AtomicReference<List<String>> visible = new AtomicReference<>();
        AtomicReference<Document> after = new AtomicReference<>(new Document("not asked yet"));

        onFx(() -> {
            windowManager.setFocusedView(behindView);
            windowManager.showView(console);            // in front of the document now
            windowManager.setFocusedView(closingView);
            visible.set(visibleViewIds());
            windowManager.closeView(closingView);
            after.set(activeDocument());
        });

        assertThat(visible.get(), hasItem("console"));
        assertThat("nothing the user can see has a context to offer", after.get(), is(nullValue()));
    }

    @Test
    public void theMostRecentOfSeveralRemainingDocumentsTakesOver() throws Exception {
        Document first = new Document("first");
        Document second = new Document("second");
        Document third = new Document("third");
        View firstView = documentView("first", Position.CENTER, first);
        View secondView = documentView("second", Position.RIGHT, second);
        View thirdView = documentView("third", Position.BOTTOM, third);
        onFx(() -> {
            windowManager.register(firstView);
            windowManager.register(secondView, firstView);
            windowManager.register(thirdView, firstView);
        });
        AtomicReference<Document> afterThird = new AtomicReference<>();
        AtomicReference<Document> afterFirst = new AtomicReference<>();

        onFx(() -> {
            // The user worked in the second document, then the first, then the third.
            windowManager.setFocusedView(secondView);
            windowManager.setFocusedView(firstView);
            windowManager.setFocusedView(thirdView);
            windowManager.closeView(thirdView);
            afterThird.set(activeDocument());
            windowManager.closeView(firstView);
            afterFirst.set(activeDocument());
        });

        assertThat(afterThird.get(), is(first));
        assertThat(afterFirst.get(), is(second));
    }

    @Test
    public void aToolShowingADocumentIsToldOnceWhenThatDocumentCloses() throws Exception {
        Document left = new Document("left");
        Document right = new Document("right");
        View leftView = documentView("left", Position.CENTER, left);
        View rightView = documentView("right", Position.RIGHT, right);
        View outline = silentTool("outline", Position.LEFT);
        onFx(() -> {
            windowManager.register(leftView);
            windowManager.register(rightView, leftView);
            windowManager.register(outline, leftView);
        });
        List<Document> seen = new ArrayList<>();
        AtomicReference<Document> whileInOutline = new AtomicReference<>();

        onFx(() -> {
            windowManager.setFocusedView(leftView);
            windowManager.setFocusedView(rightView);
            // The user now works in the outline, which shows the right document.
            windowManager.setFocusedView(outline);
            whileInOutline.set(activeDocument());
            LookupContext.Subscription subscription =
                    windowManager.getActiveLookup().observe(Document.class, seen::addAll);
            windowManager.closeView(rightView);
            subscription.cancel();
        });

        assertThat(whileInOutline.get(), is(right));
        assertThat("no empty state in between", seen, contains(left));
    }

    /* ----- one document, several views -------------------------------------- */

    @Test
    public void twoViewsOfOneDocumentDoNotReportAChangeOfDocument() throws Exception {
        Document document = new Document("shared");
        View overview = documentView("overview", Position.CENTER, document, "overview");
        View detail = documentView("detail", Position.RIGHT, document, "detail");
        onFx(() -> {
            windowManager.register(overview);
            windowManager.register(detail, overview);
        });
        List<Document> documentChanges = new ArrayList<>();
        List<String> viewChanges = new ArrayList<>();
        AtomicReference<Document> afterClose = new AtomicReference<>();

        onFx(() -> {
            windowManager.setFocusedView(detail);
            LookupContext.Subscription documents =
                    windowManager.getActiveLookup().observe(Document.class, documentChanges::addAll);
            LookupContext.Subscription views =
                    windowManager.getActiveLookup().observe(String.class, viewChanges::addAll);
            windowManager.setFocusedView(overview);
            windowManager.setFocusedView(detail);
            windowManager.closeView(detail);
            afterClose.set(activeDocument());
            documents.cancel();
            views.cancel();
        });

        assertThat("the same document all along", documentChanges, is(empty()));
        assertThat(viewChanges, contains("overview", "detail", "overview"));
        assertThat(afterClose.get(), is(document));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void closedDocumentsAreNotRemembered() throws Exception {
        View first = documentView("first", Position.CENTER, new Document("first"));
        View second = documentView("second", Position.RIGHT, new Document("second"));
        onFx(() -> {
            windowManager.register(first);
            windowManager.register(second, first);
        });

        onFx(() -> {
            windowManager.setFocusedView(first);
            windowManager.setFocusedView(second);
            windowManager.closeView(second);
            windowManager.closeView(first);
        });

        List<View> remembered = (List<View>) FieldUtils.readField(windowManager, "activeLookupHistory", true);
        assertThat("a closed document must be free to go", remembered, is(empty()));
    }
}
