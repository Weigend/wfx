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
package io.softwareecg.wfx.windowmanager.api;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.scene.Parent;

import java.util.List;
import java.util.Optional;

/**
 * A Window manager which is able to handle views dynamically.
 *
 */
public interface WindowManager {

    /**
     * Initialize the window manager.
     */
    void init();

    /**
     * Register and show a new view within this window manager.
     * <p>
     * The Position will give an advice where this view should be placed.
     *
     * @param view The view to register.
     */
    default void register(View view) {
        register(view, true);
    }

    /**
     * Register a new view within this window manager.
     * <p>
     * The Position will give an advice where this view should be placed.
     * <p>
     * If {@code showView} is {@code true} the new view is also made the focused view
     * (see {@link #focusedViewProperty()}). JavaFX's focus owner only moves on user
     * interaction, so without this consumers would have to call
     * {@link #setFocusedView(View)} explicitly to get focus-driven side panels to
     * rebind to the freshly opened view.
     *
     * @param view     The view to register.
     * @param showView True if the view should be shown immediately, false otherwise
     */
    void register(View view, boolean showView);

    /**
     * Register and a new view within this window manager using a parent view to define the exact position.
     * <p>
     * It use the given parent view with the views position to exactly define the displayed position. If the position is
     * {@link Position#CENTER} the registered view will be placed as tab next to the parent view. In any other position
     * value the area which contains the parent view will be split according to the value of position of the new view.
     *
     * @param view   The view to register.
     * @param parent An already registered view which defines the exact position to insert the view.
     */
    default void register(View view, View parent) {
        register(view, parent, true);
    }

    /**
     * Register a new view within this window manager using a parent view to define the exact position.
     * <p>
     * It use the given parent view with the views position to exactly define the displayed position. If the position is
     * {@link Position#CENTER} the registered view will be placed as tab next to the parent view. In any other position
     * value the area which contains the parent view will be split according to the value of position of the new view.
     * <p>
     * If {@code showView} is {@code true} the new view is also made the focused view
     * (see {@link #focusedViewProperty()}).
     *
     * @param view     The view to register.
     * @param parent   An already registered view which defines the exact position to insert the view.
     * @param showView True if the view should be shown immediately, false otherwise
     */
    void register(View view, View parent, boolean showView);

    /**
     * Get the root pane for this window manager.
     *
     * @return The root pane.
     */
    Parent getRootPane();

    /**
     * Restore the default layout according to the views position and insertion order.
     */
    void restoreDefaultLayout();

    /**
     * Close the specified view.
     * <p>
     * The given view must be registered within the {@link WindowManager}. If the view is not registered it returns
     * false.
     *
     * @param view That view that should be closed
     * @return true if the view was successfully closed. otherwise false.
     */
    boolean closeView(View view);

    /**
     * Unregister the given view.
     * <p>
     * If the given view is currently visible it will be closed and then removed from the list of registered views.
     *
     * @param view The view to register.
     * @return true if the view was successfully closed and unregistered. otherwise false.
     */
    boolean unregister(View view);

    /**
     * Show a closed view again. The view will be shown at the same position where it was on close. The given view must
     * be registered within the {@link WindowManager}. If it is not registered a {@link IllegalArgumentException} will
     * be thrown.
     *
     * @param view The view to show.
     */
    void showView(View view);

    /**
     * Find a view with the assigned view id.
     * <p>
     * This returns that view that has the given unique view id. If there is no view found it returns null.
     *
     * @param viewID The view id to search.
     * @return The registered view or null if it was not found.
     * @see #findViewById(String) for an Optional-returning alternative
     */
    View findView(String viewID);

    /**
     * Find a view with the assigned view id.
     * <p>
     * This is the {@link Optional}-returning alternative to {@link #findView(String)}.
     *
     * @param viewID The view id to search.
     * @return An Optional containing the registered view, or empty if not found.
     * @since 6.3.0
     */
    default Optional<View> findViewById(String viewID) {
        return Optional.ofNullable(findView(viewID));
    }

    /**
     * Get that view that currently holds the focus within this window.
     *
     * @return That view that holds the focus, or null if no view has focus.
     * @see #currentFocusedView() for an Optional-returning alternative
     */
    View getFocusedView();

    /**
     * Get the view that currently holds the focus within this window.
     * <p>
     * This is the {@link Optional}-returning alternative to {@link #getFocusedView()}.
     *
     * @return An Optional containing the focused view, or empty if no view has focus.
     * @since 6.3.0
     */
    default Optional<View> currentFocusedView() {
        return Optional.ofNullable(getFocusedView());
    }

    /**
     * Get that view that holds recently the focus within this window..
     *
     * @return That view that holds recently the focus, or null if none.
     * @see #previousFocusedView() for an Optional-returning alternative
     */
    View getLastFocusedView();

    /**
     * Get the view that most recently held focus within this window.
     * <p>
     * This is the {@link Optional}-returning alternative to {@link #getLastFocusedView()}.
     *
     * @return An Optional containing the previously focused view, or empty if none.
     * @since 6.3.0
     */
    default Optional<View> previousFocusedView() {
        return Optional.ofNullable(getLastFocusedView());
    }

    /**
     * Set the given view as the view that holds currently the focus.
     *
     * @param view The view that should hold the focus.
     */
    void setFocusedView(View view);

    /**
     * Get the focused view property.
     *
     * @return the focused view property.
     */
    ObjectProperty<View> focusedViewProperty();

    /**
     * Get all views that are visible at the moment.
     *
     * @return Get all visible views.
     */
    List<View> getVisibleViews();

    /**
     * Get an {@link javafx.collections.ObservableList} with all currently registered views.
     *
     * @return a list with all registered views.
     */
    ReadOnlyListProperty<View> getRegisteredViews();

    /**
     * Get the registry of {@link ViewKind#TOOL TOOL} views in the order they
     * were first registered.
     * <p>
     * Stable across {@link #closeView(View) closeView} (closing a tool only
     * hides it) — useful for an auto-built "View" menu that lists every
     * available tool panel. Removed only by an explicit
     * {@link #unregister(View)} or by registering a brand-new tool with the
     * same {@code viewId}. {@link ViewKind#DOCUMENT DOCUMENT} views never
     * appear in this list.
     *
     * @return read-only list of TOOL views in registration order.
     * @since 1.1.0
     */
    ReadOnlyListProperty<View> getToolViews();

    /**
     * Check if the given view is already registered.
     *
     * @param view The view to check.
     * @return true if the view is registered. otherwise false
     */
    boolean hasRegisteredView(View view);

    /**
     * Check if the given view is registered and currently visible.
     *
     * @param view The view to check.
     * @return true if the view is visible. otherwise false.
     */
    boolean hasVisibleView(View view);

    /**
     * Get the window factory to create new managed windows.
     *
     * @return The window factory to use.
     */
    WindowFactory getWindowFactory();

    /**
     * Set the window factory to create new managed windows.
     *
     * @param factory The new window factory.
     */
    void setWindowFactory(WindowFactory factory);
}
