/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 Weigend AM
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
package io.softwareecg.wfx.extension.systemviews;

import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.windowmanager.api.ApplicationWindow;
import io.softwareecg.wfx.windowmanager.api.FXMLView;
import io.softwareecg.wfx.windowmanager.api.Position;
import io.softwareecg.wfx.windowmanager.api.WindowManager;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.softwareecg.wfx.extension.uiutils.MenuUtil.*;

/**
 * Helper class to register the system views.
 *
 */
public final class SystemViewsHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemViewsHelper.class);

    private SystemViewsHelper() {
    }

    /**
     * Get the legacy "View &rarr; Windows" submenu.
     *
     * @return the windows menu.
     * @deprecated since 1.0.1 — prefer {@link #getWindowMenu()} for a top-level
     * "Window" menu in the workbench convention.
     */
    @Deprecated(since = "1.0.1", forRemoval = false)
    public static Menu getWindowsMenu() {
        final ApplicationWindow appWindow = Lookup.lookup(ApplicationWindow.class);
        ObservableList<Menu> menu = appWindow.getMenu();
        Menu view = findOrCreateItem(menu, "view", () -> new Menu("View"), 2);
        return (Menu) findOrCreateItem(view.getItems(), "windows", () -> new Menu("Windows"), 0);
    }

    /**
     * Add a "Views overview" entry to the legacy "View &rarr; Windows" menu.
     *
     * @deprecated since 1.0.1 — prefer {@link #addStandardWindowActions()},
     * which adds the entry plus "Restore Default Layout" to the top-level
     * "Window" menu.
     */
    @Deprecated(since = "1.0.1", forRemoval = false)
    public static void addViewOverview() {
        try {
            FXMLView<ViewOverview> overview = buildOverviewView();
            findOrCreateItem(getWindowsMenu().getItems(), "viewOverview",
                    () -> createMenuItem("Views overview", showView(overview)), Integer.MAX_VALUE);
        }
        catch (IOException e) {
            LOGGER.error("Unable to create view", e);
        }
    }

    /**
     * Get (or create) the top-level "Window" menu. Standard workbench location
     * for layout and view-management actions.
     *
     * @return the top-level Window menu.
     * @since 1.0.1
     */
    public static Menu getWindowMenu() {
        final ApplicationWindow appWindow = Lookup.lookup(ApplicationWindow.class);
        ObservableList<Menu> menu = appWindow.getMenu();
        return findOrCreateItem(menu, "window", () -> new Menu("Window"), Integer.MAX_VALUE);
    }

    /**
     * Add "Restore Default Layout" to the top-level Window menu. The action
     * delegates to {@link WindowManager#restoreDefaultLayout()}.
     *
     * @since 1.0.1
     */
    public static void addRestoreDefaultLayoutAction() {
        findOrCreateItem(getWindowMenu().getItems(), "restoreDefaultLayout",
                () -> createMenuItem("Restore Default Layout",
                        e -> Lookup.lookup(WindowManager.class).restoreDefaultLayout()),
                0);
    }

    /**
     * Populate the top-level Window menu with the standard workbench actions:
     * "Restore Default Layout" followed by "Views Overview", separated.
     *
     * @since 1.0.1
     */
    public static void addStandardWindowActions() {
        addRestoreDefaultLayoutAction();
        Menu windowMenu = getWindowMenu();
        findOrCreateItem(windowMenu.getItems(), "windowSeparator1",
                SeparatorMenuItem::new, 1);
        try {
            FXMLView<ViewOverview> overview = buildOverviewView();
            findOrCreateItem(windowMenu.getItems(), "viewOverview",
                    () -> createMenuItem("Views Overview", showView(overview)),
                    Integer.MAX_VALUE);
        }
        catch (IOException e) {
            LOGGER.error("Unable to create Views Overview view", e);
        }
    }

    private static FXMLView<ViewOverview> buildOverviewView() throws IOException {
        return new FXMLView.Builder<ViewOverview>()
                .withId("viewOverview:1")
                .withTitle("Views Overview")
                .withPos(Position.RIGHT)
                .withViewAreaSize(0.3)
                .withFile(ViewOverview.class.getResource("ViewOverview.fxml"))
                .build();
    }
}
