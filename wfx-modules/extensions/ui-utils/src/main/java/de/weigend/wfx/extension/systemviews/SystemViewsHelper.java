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
package de.weigend.wfx.extension.systemviews;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.windowmtg.api.ApplicationWindow;
import de.weigend.wfx.windowmtg.api.FXMLView;
import de.weigend.wfx.windowmtg.api.Position;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static de.weigend.wfx.extension.uiutils.MenuUtil.*;

/**
 * Helper class to register the system views.
 *
 */
public final class SystemViewsHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemViewsHelper.class);

    private SystemViewsHelper() {
    }

    /**
     * Get the windows menu.
     *
     * @return the windows menu.
     */
    public static Menu getWindowsMenu() {
        final ApplicationWindow appWindow = Lookup.lookup(ApplicationWindow.class);
        ObservableList<Menu> menu = appWindow.getMenu();
        Menu view = findOrCreateItem(menu, "view", () -> new Menu("View"), 2);
        return (Menu) findOrCreateItem(view.getItems(), "windows", () -> new Menu("Windows"), 0);
    }

    /**
     * Add the menu item to show the views overview.
     */
    public static void addViewOverview() {
        try {
            FXMLView<ViewOverview> overview = new FXMLView.Builder<ViewOverview>()
                    .withId("viewOverview:1")
                    .withTitle("Views Overview")
                    .withPos(Position.RIGHT)
                    .withViewAreaSize(0.3)
                    .withFile(ViewOverview.class.getResource("ViewOverview.fxml"))
                    .build();
            findOrCreateItem(getWindowsMenu().getItems(), "viewOverview",
                    () -> createMenuItem("Views overview", showView(overview)), Integer.MAX_VALUE);
        }
        catch (IOException e) {
            LOGGER.error("Unable to create view", e);
        }
    }
}
