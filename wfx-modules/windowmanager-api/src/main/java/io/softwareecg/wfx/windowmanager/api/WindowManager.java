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

import io.softwareecg.wfx.lookup.api.LookupContext;
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
     * The Position will give an advice where this view should be placed:
     * which side, and the {@linkplain View#getDefaultAnchor() anchor} of the
     * view says of what — the content of the main window, or the editor pane
     * alone ({@link ViewAnchor#EDITOR}).
     * <p>
     * If {@code showView} is {@code true} the new view is also made the focused view
     * (see {@link #focusedViewProperty()}). JavaFX's focus owner only moves on user
     * interaction, so without this consumers would have to call
     * {@link #setFocusedView(View)} explicitly to get focus-driven side panels to
     * rebind to the freshly opened view.
     * <p>
     * A shown {@link ViewKind#DOCUMENT DOCUMENT} view is also brought to the
     * front of its pane: it was opened on request, and what holds the focus
     * should be what the user sees. A {@link ViewKind#TOOL TOOL} view that
     * joins a pane with other tabs stays behind the selected one, so that the
     * tools registered into one pane at startup show the first of them.
     *
     * @param view     The view to register.
     * @param showView True if the view should be shown immediately, false otherwise
     */
    void register(View view, boolean showView);

    /**
     * Register and a new view within this window manager using a parent view to define the exact position.
     * <p>
     * It use the given parent view with the views position to exactly define the displayed position. If the position is
     * {@link Position#CENTER} the registered view will be placed as tab right behind the tab of the parent view, and
     * behind the tabs placed next to the same parent before; closing it brings the parent back to the front. In any
     * other position value the area which contains the parent view will be split according to the value of position
     * of the new view.
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
     * {@link Position#CENTER} the registered view will be placed as tab right behind the tab of the parent view, and
     * behind the tabs placed next to the same parent before; closing it brings the parent back to the front. In any
     * other position value the area which contains the parent view will be split according to the value of position
     * of the new view.
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
     * <p>
     * Every {@link ViewKind#DOCUMENT DOCUMENT} view is closed and unregistered,
     * every {@link ViewKind#TOOL TOOL} view goes back to where it was first
     * registered. A tool that was registered next to a document — a panel
     * docked under the first editor tab, say — outlives that document: it is
     * placed where the document would go, which for a document at
     * {@link Position#CENTER} is next to the editor pane.
     */
    void restoreDefaultLayout();

    /**
     * Capture the current arrangement of the views as text, so that it can be
     * stored and brought back with {@link #restoreLayout(String)} — typically
     * on the next start of the application.
     * <p>
     * The layout records which views share a tab pane and in which order, how
     * the panes are split and where the dividers sit, which tab is selected,
     * which views are hidden, and which views live in detached windows
     * together with the position and size of those windows. Views are
     * referred to by their {@linkplain View#getViewId() id}. The position and
     * size of the main window are not part of it; the main window belongs to
     * the {@link ApplicationWindow}.
     * <p>
     * Call this on the JavaFX Application Thread, for example from
     * {@code Module.stop()}.
     *
     * @return the layout as text; where to keep it is up to the application
     */
    String saveLayout();

    /**
     * Arrange the registered views as described by a layout that
     * {@link #saveLayout()} produced.
     * <p>
     * Only views that are registered at the time of the call can be placed, so
     * call this after the views have been registered — for example from
     * {@code Module.start()}. The layout and the registry need not match:
     * <ul>
     *     <li>a view the layout mentions but that is not registered (a
     *     document that was not reopened, a tool that no longer exists) is
     *     skipped, and a pane or window left empty by that is dropped;</li>
     *     <li>a registered view the layout does not know (a tool added by a
     *     newer version of the application) keeps its current state: if it is
     *     shown, it is placed at its default position.</li>
     * </ul>
     * When called from another thread the layout is applied later on the
     * JavaFX Application Thread.
     *
     * @param layout the text returned by {@link #saveLayout()}
     * @return {@code true} if the layout was understood and is (being)
     *         applied; {@code false} if the text is not a layout this version
     *         can read, in which case the current arrangement is left alone
     */
    boolean restoreLayout(String layout);

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
     * <p>
     * A view that was registered next to a parent view is shown next to that parent again. If the parent is not shown
     * any more — hidden, or gone for good like a closed document — the view is placed where the parent would go.
     * A view without a parent is placed by its position and its {@linkplain View#getDefaultAnchor() anchor}.
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
     * <p>
     * A view that is closed or unregistered does not keep the focus. It goes
     * back to the view that held it most recently and can still be seen — its
     * tab is the selected one of its pane —, and to nobody if there is none.
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
     * @return That view that holds recently the focus, or null if none — also
     *         if that view has been closed or unregistered since.
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
     * Get the context of the view the user is working in.
     * <p>
     * The returned object stays the same for the lifetime of the window
     * manager; what it contains follows the user. When a view gets the focus,
     * its {@link View#getLookup() context} becomes the active one — unless the
     * view does not take part (its context is
     * {@link LookupContext#EMPTY}), in which case the active context is left
     * as it is, so that for example looking at a console view does not
     * disable the actions for the current selection.
     * <p>
     * When the view that supplies the active context is closed or
     * unregistered, the context goes back to the view that supplied it before
     * and can still be seen — a view whose tab lies behind another one is
     * passed over. Closing one of two documents shown side by side therefore
     * leaves the other one active, without the user having to click into it.
     * If no such view is left, the active context becomes empty.
     * <p>
     * Several views can show the same thing: each of them puts the same
     * object — the document — into its context. Whoever observes that type
     * here is then told when the user moves to another document, and not when
     * they move between two views of the same one.
     * <p>
     * This is the place to observe for anything that depends on "the current
     * selection" regardless of the view it was made in:
     * <pre>{@code
     * windowManager.getActiveLookup().observe(Project.class,
     *         projects -> closeProjectItem.setDisable(projects.isEmpty()));
     * }</pre>
     *
     * @return the active context, never null
     */
    LookupContext getActiveLookup();

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
