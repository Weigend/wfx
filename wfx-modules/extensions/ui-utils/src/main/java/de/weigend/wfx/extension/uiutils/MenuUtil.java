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
package de.weigend.wfx.extension.uiutils;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.windowmtg.api.View;
import de.weigend.wfx.windowmtg.api.WindowManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

/**
 * This class contains some utils for easier handling of javafx' {@link Menu Menus} and {@link MenuItem MenuItems}.
 *
 * @author Software-EKG Team
 */
public final class MenuUtil {
    private MenuUtil() {
    }

    /**
     * Create a new {@link MenuItem} with the given settings.
     *
     * @param name         The visible name
     * @param clickHandler The event handler listening for mouse clicks.
     * @return the created menu item.
     */
    public static MenuItem createMenuItem(String name, EventHandler<ActionEvent> clickHandler) {
        MenuItem menuItem = new MenuItem(name);
        menuItem.setOnAction(clickHandler);
        return menuItem;
    }

    /**
     * Create a new {@link MenuItem} with the given settings.
     *
     * @param id           The id for the new item
     * @param name         The visible name
     * @param clickHandler The event handler listening for mouse clicks.
     * @return the created menu item.
     */
    public static MenuItem createMenuItem(String id, String name, EventHandler<ActionEvent> clickHandler) {
        MenuItem menuItem = new MenuItem(name);
        menuItem.setId(id);
        menuItem.setOnAction(clickHandler);
        return menuItem;
    }

    /**
     * Create a new {@link Menu} with the given id and name.
     *
     * @param id   the id for the new menu
     * @param name the visible name for the menu
     * @return the created menu
     */
    public static Menu createMenu(String id, String name) {
        Menu menu = new Menu(name);
        menu.setId(id);
        return menu;
    }

    /**
     * Find a {@link MenuItem} within the given list. If not found it returns null.
     *
     * @param parentMenu The list to search the item.
     * @param id         The id of the searched item.
     * @param recursive  Should the search recursive?
     * @return The found menu item or null if not found.
     */
    public static MenuItem findItem(List<? extends MenuItem> parentMenu, String id, boolean recursive) {
        Objects.requireNonNull(parentMenu);
        Objects.requireNonNull(id);

        for (MenuItem item : parentMenu) {
            if (StringUtils.equals(id, item.getId())) {
                return item;
            }
            else if (recursive && item instanceof Menu) {
                MenuItem menuItem = findItem(((Menu) item).getItems(), id, true);
                if (menuItem != null) {
                    return menuItem;
                }
            }
        }
        return null;
    }

    /**
     * Try to find an {@link MenuItem} with the requested id or create a new item with the same id.
     * <p>
     * This method first try to find an {@link MenuItem} with the given id within the given parent menu list. If it find
     * some {@link MenuItem}, the {@link MenuItem} are returned. Otherwise it creates a new {@link MenuItem} using the
     * given {@link Supplier}, adjust the id and place it on the requested position.
     *
     * @param parentMenu The list of items to search and may add the item.
     * @param id         The id to search.
     * @param createItem A supplier to create a new item.
     * @param pos        The position for the new item.
     * @param <T>        The type of items.
     * @return The found or new created item.
     */
    public static <T extends MenuItem> T findOrCreateItem(List<T> parentMenu, String id, Supplier<? extends T> createItem, int pos) {
        MenuItem item = findItem(parentMenu, id, false);

        if (item == null) {
            T newItem = createItem.get();
            newItem.setId(id);
            int min = min(max(0, pos), parentMenu.size());
            parentMenu.add(min, newItem);
            return newItem;
        }
        else {
            return (T) item;
        }
    }

    /**
     * Get an {@link EventHandler} that shows the given view.
     *
     * @param view The view to show.
     * @return the event handler that shows the view.
     */
    public static EventHandler<ActionEvent> showView(View view) {
        return event -> {
            final WindowManager manager = Lookup.lookup(WindowManager.class);
            if (!manager.hasRegisteredView(view)) {
                manager.register(view);
            }
            else {
                manager.showView(view);
            }
        };
    }
}
