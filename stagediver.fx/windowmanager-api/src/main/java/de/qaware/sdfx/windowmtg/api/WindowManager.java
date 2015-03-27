// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 21.06.13 10:22
//     description: The window manager.
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.api;

import javafx.scene.Parent;

import java.util.List;

/**
 * A Window manager which is able to handle views dynamically.
 *
 * @author christian.fritz
 */
public interface WindowManager {

    /**
     * Initialize the window manager.
     */
    void init();

    /**
     * Register a new view within this window manager.
     * <p>
     * The Position will give an advice where this view should be placed.
     *
     * @param view The view to register.
     */
    void register(View view);

    /**
     * Register a new view within this window manager using a parent view to define the exact position.
     * <p>
     * It use the given parent view with the views position to exactly define the displayed position. If the position is
     * {@link Position#CENTER} the registered view will be placed as tab next to the parent view. In any other position
     * value the area which contains the parent view will be split according to the value of position of the new view.
     *
     * @param view   The view to register.
     * @param parent An already registered view which defines the exact position to insert the view.
     */
    void register(View view, View parent);

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
     * The given view must be registered within the {@link WindowManager}. If it is not registered a
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param view That view that should be closed
     */
    void closeView(View view);

    /**
     * Clone the specified view.
     * <p>
     * The cloned view will be placed next to the given view in the same tab area.
     * <p>
     * The given view must be registered within the {@link WindowManager}. If it is not registered a
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param view Clone the given view.
     * @return The cloned view object.
     */
    View cloneView(View view);

    /**
     * Show a closed view again.
     * The view will be shown at the same position where it was on close.
     * The given view must be registered within the {@link WindowManager}. If it is not registered a
     * {@link IllegalArgumentException} will be thrown.
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
     */
    View findView(String viewID);

    /**
     * Get that view that currently holds the focus within this window.
     *
     * @return That view that holds the focus.
     */
    View getFocusedView();

    /**
     * Get that view that holds recently the focus within this window..
     *
     * @return That view that hodls recently the focus.
     */
    View getLastFocusedView();

    /**
     * Set the given view as the view that holds currently the focus.
     *
     * @param view The view that should hold the focus.
     */
    void setFocusedView(View view);

    /**
     * Get all views that are visible at the moment.
     *
     * @return Get all visible views.
     */
    List<View> getVisibleViews();
}
