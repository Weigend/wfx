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

import io.softwareecg.wfx.extension.uiutils.MenuUtil;
import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.platform.api.Module;
import io.softwareecg.wfx.windowmtg.api.ApplicationWindow;
import io.softwareecg.wfx.windowmtg.api.View;
import io.softwareecg.wfx.windowmtg.api.WindowManager;
import jakarta.annotation.Priority;
import jakarta.inject.Singleton;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 * Auto-builds the top-level "View" menu, with one entry per registered
 * {@link io.softwareecg.wfx.windowmtg.api.ViewKind#TOOL TOOL} view.
 * <p>
 * Clicking an entry shows the corresponding view: a hidden TOOL is brought
 * back to its last known position, a visible one is focused. Module
 * priority is intentionally chosen to run after modules that register
 * their own default views, so the initial menu build sees them all.
 * Subsequent registrations are picked up via a {@link ListChangeListener}
 * on {@link WindowManager#getToolViews()}.
 *
 * @since 1.1.0
 */
@Singleton
@Priority(50)
public class ViewMenuModule implements Module {

    private static final String VIEW_MENU_ID = "view";
    private static final String VIEW_MENU_TITLE = "View";

    @Override
    public void preload() {
        // No preload work — the menu is built in start() once the
        // application window and the window manager are wired up.
    }

    @Override
    public void start() {
        ApplicationWindow appWindow = Lookup.lookup(ApplicationWindow.class);
        WindowManager wm = Lookup.lookup(WindowManager.class);

        Menu viewMenu = MenuUtil.findOrCreateItem(
                appWindow.getMenu(), VIEW_MENU_ID, () -> new Menu(VIEW_MENU_TITLE), 1);

        rebuildItems(viewMenu, wm);
        wm.getToolViews().addListener(
                (ListChangeListener<View>) c -> rebuildItems(viewMenu, wm));
    }

    @Override
    public void stop() {
        // The menu lives on the application window; it disappears together
        // with the stage. No explicit teardown is required.
    }

    /**
     * Rebuild the menu in place from the current tool-view registry. Called
     * on initial start and on every change to {@link WindowManager#getToolViews()}.
     * <p>
     * Items are recreated rather than diffed because the registry is small
     * (typically &lt;10 entries) and clear-and-rebuild keeps the ordering
     * exactly aligned with the registry.
     */
    private static void rebuildItems(Menu viewMenu, WindowManager wm) {
        viewMenu.getItems().clear();
        for (View v : wm.getToolViews()) {
            MenuItem item = MenuUtil.createMenuItem(
                    "view." + v.getViewId(),
                    v.getTitle(),
                    e -> wm.showView(v));
            viewMenu.getItems().add(item);
        }
    }
}
