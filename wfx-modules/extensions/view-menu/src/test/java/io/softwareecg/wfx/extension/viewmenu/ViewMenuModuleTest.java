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
package io.softwareecg.wfx.extension.viewmenu;

import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.lookup.api.LookupStrategy;
import io.softwareecg.wfx.windowmanager.api.ApplicationWindow;
import io.softwareecg.wfx.windowmanager.api.View;
import io.softwareecg.wfx.windowmanager.api.WindowManager;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ViewMenuModule}.
 * <p>
 * Drives the module against a mocked {@link ApplicationWindow} and
 * {@link WindowManager}, with a real {@link ReadOnlyListWrapper} backing the
 * tool-views property so that change-listener wiring is exercised end-to-end.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewMenuModuleTest {

    private static boolean fxStarted;

    @BeforeClass
    public static void initFx() {
        // The first Menu/MenuItem instantiation goes through JavaFX'
        // CSS / styling subsystem, which expects the JavaFX runtime to be
        // up. Boot it once for the test class — no Stage needed since we
        // only ever build menus, never show them. Platform.startup throws
        // on the second call across the JVM, so guard against that.
        if (!fxStarted) {
            try {
                Platform.startup(() -> { });
            }
            catch (IllegalStateException alreadyRunning) {
                // FX runtime already up — fine, that's exactly what we need.
            }
            fxStarted = true;
        }
    }

    @Mock
    private ApplicationWindow appWindow;

    @Mock
    private WindowManager windowManager;

    @Mock
    private LookupStrategy lookupStrategy;

    private ObservableList<Menu> menuBar;
    private ObservableList<View> toolViewsBacking;

    private ViewMenuModule module;

    @Before
    public void setUp() {
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(ApplicationWindow.class)).thenReturn(appWindow);
        when(lookupStrategy.lookup(WindowManager.class)).thenReturn(windowManager);

        menuBar = FXCollections.observableArrayList();
        when(appWindow.getMenu()).thenReturn(menuBar);

        toolViewsBacking = FXCollections.observableArrayList();
        ReadOnlyListProperty<View> toolViewsProp = new ReadOnlyListWrapper<>(
                this, "toolViews", toolViewsBacking).getReadOnlyProperty();
        when(windowManager.getToolViews()).thenReturn(toolViewsProp);

        module = new ViewMenuModule();
    }

    @Test
    public void testStartCreatesViewMenuAndPopulatesFromToolRegistry() {
        toolViewsBacking.add(mockView("explorer", "Explorer"));
        toolViewsBacking.add(mockView("logger", "Logger Console"));

        module.start();

        Menu viewMenu = findViewMenu();
        assertThat(viewMenu, is(notNullValue()));
        assertThat(viewMenu.getItems(), hasSize(2));
        assertThat(viewMenu.getItems().get(0).getText(), is(equalTo("Explorer")));
        assertThat(viewMenu.getItems().get(1).getText(), is(equalTo("Logger Console")));
    }

    @Test
    public void testMenuItemsPreserveRegistrationOrder() {
        // Insertion order on the tool registry IS the menu order — predictable
        // for users, no surprise alphabetical sort.
        toolViewsBacking.add(mockView("c", "Charlie"));
        toolViewsBacking.add(mockView("a", "Alpha"));
        toolViewsBacking.add(mockView("b", "Bravo"));

        module.start();

        Menu viewMenu = findViewMenu();
        assertThat(viewMenu.getItems(), hasSize(3));
        assertThat(viewMenu.getItems().get(0).getText(), is(equalTo("Charlie")));
        assertThat(viewMenu.getItems().get(1).getText(), is(equalTo("Alpha")));
        assertThat(viewMenu.getItems().get(2).getText(), is(equalTo("Bravo")));
    }

    @Test
    public void testMenuItemIdsUseViewIdNamespace() {
        // Stable id => other code can look up / re-target items via MenuUtil.findItem.
        toolViewsBacking.add(mockView("explorer", "Explorer"));

        module.start();

        MenuItem item = findViewMenu().getItems().get(0);
        assertThat(item.getId(), is(equalTo("view.explorer")));
    }

    @Test
    public void testClickingMenuItemShowsTheView() {
        // The whole point: a closed TOOL is brought back via showView,
        // which both re-displays a hidden one and focuses an already-visible one.
        View explorer = mockView("explorer", "Explorer");
        toolViewsBacking.add(explorer);

        module.start();
        findViewMenu().getItems().get(0).fire();

        verify(windowManager).showView(explorer);
    }

    @Test
    public void testRebuildOnLateRegistration() {
        // A plug-in registers a TOOL view AFTER ViewMenuModule.start() has run.
        // The list listener must catch the change and append the new entry.
        module.start();
        assertThat(findViewMenu().getItems(), hasSize(0));

        toolViewsBacking.add(mockView("late", "Late Comer"));

        Menu viewMenu = findViewMenu();
        assertThat(viewMenu.getItems(), hasSize(1));
        assertThat(viewMenu.getItems().get(0).getText(), is(equalTo("Late Comer")));
    }

    @Test
    public void testRebuildOnUnregister() {
        // unregister(toolView) drops the view from the tool registry, so the
        // menu entry should disappear too — keeps "what's in the menu" honest.
        View a = mockView("a", "Alpha");
        View b = mockView("b", "Bravo");
        toolViewsBacking.addAll(a, b);

        module.start();
        assertThat(findViewMenu().getItems(), hasSize(2));

        toolViewsBacking.remove(a);

        assertThat(findViewMenu().getItems(), hasSize(1));
        assertThat(findViewMenu().getItems().get(0).getText(), is(equalTo("Bravo")));
    }

    @Test
    public void testStartReusesExistingViewMenu() {
        // If the application has already created a "view" menu (e.g. an app-
        // specific module added a custom item earlier), ViewMenuModule must
        // populate THAT menu rather than creating a duplicate.
        Menu existingViewMenu = new Menu("View");
        existingViewMenu.setId("view");
        menuBar.add(existingViewMenu);

        toolViewsBacking.add(mockView("explorer", "Explorer"));

        module.start();

        long viewMenuCount = menuBar.stream()
                .filter(m -> "view".equals(m.getId()))
                .count();
        assertThat(viewMenuCount, is(equalTo(1L)));
        assertThat(existingViewMenu.getItems(), hasSize(1));
    }

    private Menu findViewMenu() {
        return menuBar.stream()
                .filter(m -> "view".equals(m.getId()))
                .findFirst()
                .orElse(null);
    }

    private static View mockView(String id, String title) {
        View view = mock(View.class);
        when(view.getViewId()).thenReturn(id);
        when(view.getTitle()).thenReturn(title);
        return view;
    }
}
