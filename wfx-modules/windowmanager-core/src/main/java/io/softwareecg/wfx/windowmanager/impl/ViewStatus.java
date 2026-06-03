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
package io.softwareecg.wfx.windowmanager.impl;

import io.softwareecg.wfx.windowmanager.api.Position;
import io.softwareecg.wfx.windowmanager.api.View;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Stores the current status and additional metadata of an window manager view.
 *
 */
public class ViewStatus {
    public static final int TAB_IMAGE_SIZE = 22;

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewStatus.class);
    /**
     * The registered view
     */
    private final View view;
    private final Position defaultPosition;
    private final ViewStatus parent;
    /**
     * The status whether this view is visible or hidden (or something else)
     */
    private Status status;
    /**
     * The current position within the window
     */
    private Position position;
    private TabArea area;
    /**
     * The tab which contains this view.
     */
    private Tab tab;
    /**
     * Optional hook invoked AFTER the default tab-close handling
     * (area-remove + status=HIDDEN). The {@link WindowManagerImpl} attaches
     * a handler here for {@code DOCUMENT} views so that closing the tab
     * also unregisters them. Default is a no-op so {@code TOOL} views keep
     * their historical "close hides, registry retains" behaviour.
     */
    private Consumer<ViewStatus> onTabClosed = vs -> { };

    /**
     * Create a new view status.
     *
     * @param view The view to display.
     */
    public ViewStatus(View view) {
        this(view, null);
    }

    /**
     * Create a new view status from a view with the given parent to define the exact position for this view.
     *
     * @param view   The view to display.
     * @param parent The parent view status for exact positioning.
     */
    public ViewStatus(View view, ViewStatus parent) {
        this.view = view;
        this.position = view.getDefaultPosition();
        this.defaultPosition = view.getDefaultPosition();
        this.status = Status.VISIBLE;
        this.parent = parent;
        this.initTab();
    }

    /**
     * Getter for property parent.
     *
     * @return Value for property parent.
     */
    public ViewStatus getParent() {
        return parent;
    }

    /**
     * Getter for property status.
     *
     * @return Value for property status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Setter for property status.
     *
     * @param status Value to set for property status.
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Getter for property view.
     *
     * @return Value for property view.
     */
    public View getView() {
        return view;
    }

    /**
     * Getter for property position.
     *
     * @return Value for property position.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Setter for property position.
     *
     * @param position Value to set for property position.
     */
    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * Getter for property tab.
     *
     * @return Value for property tab.
     */
    public Tab getTab() {
        return tab;
    }

    /**
     * Restore the defaults for this view.
     */
    public void restoreDefault() {
        status = Status.VISIBLE;
        position = defaultPosition;
    }

    /**
     * Initialize the javafx tab.
     */
    private void initTab() {

        tab = new Tab(view.getTitle());
        tab.setClosable(true);
        tab.setContent(view.getRootNode());
        tab.setId(view.getViewId());
        //tab.setTooltip(new Tooltip(view.getToolTipInfo()));
        tab.setUserData(this);
        if (view.getViewImagePath() != null) {
            tab.setGraphic(new ImageView(new Image(
                    view.getViewImagePath().toString(), TAB_IMAGE_SIZE, TAB_IMAGE_SIZE, true, true)));
        }
        tab.setOnClosed(event -> {
            ViewStatus viewStatus = ViewStatus.this;
            viewStatus.getArea().remove(viewStatus);
            viewStatus.setStatus(Status.HIDDEN);
            onTabClosed.accept(viewStatus);
        });
    }

    /**
     * Install a hook that runs after the tab's default close handling.
     * <p>
     * Used by the {@link WindowManagerImpl} to give
     * {@link io.softwareecg.wfx.windowmanager.api.ViewKind#DOCUMENT DOCUMENT}
     * views the additional behaviour of fully unregistering on tab-close
     * (whereas {@link io.softwareecg.wfx.windowmanager.api.ViewKind#TOOL TOOL}
     * views just hide and remain in the registry).
     *
     * @param handler the handler to invoke once the tab has been removed
     *                from its area and marked HIDDEN. Must not be
     *                {@code null}.
     */
    public void setOnTabClosed(Consumer<ViewStatus> handler) {
        this.onTabClosed = handler;
    }

    /**
     * Getter for property area.
     *
     * @return Value for property area.
     */
    public TabArea getArea() {
        return area;
    }

    /**
     * Setter for property area.
     *
     * @param area Value to set for property area.
     */
    public void setArea(TabArea area) {
        this.area = area;
    }

    /**
     * Resize the area of this view to the defined value.
     * <p>
     * The actual JavaFX call is routed through {@link #applyDividerPosition} so
     * that the requested fraction survives the SplitPane skin's first layout
     * pass even when the SplitPane has not yet been sized (a Stage that has
     * not yet been shown — typical for module preload that registers views
     * before the user-visible window is realized).
     */
    public void setDividerPositions() {
        // A closed/unattached view has no area — nothing to do.
        if (getArea() == null || getArea().getParent() == null) {
            return;
        }
        // Read the view's area size eagerly so subclass implementations get
        // the chance to compute / log / etc, regardless of whether the parent
        // turns out to host a SplitPane (mirrors the original 2013 ordering).
        final double space = getView().getViewAreaSize();
        if (!(getArea().getParent().getNode() instanceof SplitPane splitPane)) {
            return;
        }
        if (space < 0.05 || space > 0.95) {
            return;
        }
        final double target;
        switch (position) {
            case LEFT:      // fall through
            case TOP:
                target = space;
                break;
            case RIGHT:     // fall through
            case BOTTOM:
                target = 1.0 - space;
                break;
            default:
                LOGGER.warn("Invalid position {} given for setting divider positions of {}", position, splitPane);
                return;
        }
        applyDividerPosition(splitPane, target);
        LOGGER.debug("Set the divider position to {} for position {}", space, position);
    }

    /**
     * Apply {@code target} as the SplitPane's divider position robustly across
     * the SplitPane lifecycle.
     * <p>
     * JavaFX's SplitPane skin recomputes divider positions during the layout
     * pulse triggered by every size change. When views are registered before
     * the Stage is shown (typical for module preload), the SplitPane goes
     * through several size transitions (0 → intermediate → final), and on
     * each transition the skin redistributes the dividers using its default
     * fair-distribution heuristic — which clobbers any explicit
     * setDividerPositions() call. Symptom: the side panel ends up roughly
     * 50/50 regardless of the requested viewAreaSize. The fix has three parts:
     * <ol>
     *     <li>Set the value immediately so a sized SplitPane gets it right
     *         away.</li>
     *     <li>Re-apply once at the end of the next FX pulse via
     *         {@link Platform#runLater(Runnable)} to win against an in-flight
     *         layout pass.</li>
     *     <li>Install a width/height listener that re-applies the value, via
     *         {@link Platform#runLater(Runnable)}, every time the SplitPane
     *         is resized — so each redistribution by the skin is followed by
     *         our deferred write. The listener detaches after a bounded
     *         number of firings so steady-state window resizes (after the
     *         layout has stabilised) leave the user's manual divider
     *         adjustments alone.</li>
     * </ol>
     */
    private static final int MAX_REAPPLY_FIRINGS = 10;

    private static void applyDividerPosition(SplitPane splitPane, double target) {
        splitPane.setDividerPositions(target);

        // Stubbed SplitPane mocks in unit tests return null for orientation /
        // widthProperty / heightProperty; in real JavaFX both are always
        // non-null. Skip the listener-based safety net rather than NPE.
        Orientation orientation = splitPane.getOrientation();
        if (orientation == null) {
            return;
        }
        ObservableValue<Number> extent = orientation == Orientation.HORIZONTAL
                ? splitPane.widthProperty() : splitPane.heightProperty();
        if (extent == null) {
            return;
        }

        Runnable reapply = () -> splitPane.setDividerPositions(target);

        // Defer one reapply past the current FX pulse so an in-flight layout
        // pass has a chance to settle before our final write wins.
        Platform.runLater(reapply);

        // The Stage typically resizes the SplitPane in stages (0 -> intermediate
        // -> final) when first shown; the SplitPane skin recomputes divider
        // positions on each size change. A one-shot listener would only catch
        // the first transition and miss subsequent skin redistributions, so
        // keep firing until the layout has had a chance to stabilize, then
        // detach. Each firing schedules the reapply via Platform.runLater so
        // the write happens AFTER the size-driven layout pulse — that is the
        // pulse that would otherwise clobber our value.
        ChangeListener<Number> sizeListener = new ChangeListener<>() {
            int firingsLeft = MAX_REAPPLY_FIRINGS;

            @Override
            public void changed(ObservableValue<? extends Number> obs, Number old, Number newSize) {
                if (newSize.doubleValue() > 0) {
                    Platform.runLater(reapply);
                    if (--firingsLeft <= 0) {
                        extent.removeListener(this);
                    }
                }
            }
        };
        extent.addListener(sizeListener);
    }

    @Override
    public String toString() {
        return "ViewStatus{" +
                "view=" + view +
                ", parent=" + parent +
                ", status=" + status +
                ", position=" + position +
                '}';
    }

    /**
     * The status of a view.
     */
    protected enum Status {
        /**
         * Indicates that a view is visible as tab within a view area.
         */
        VISIBLE,

        /**
         * Indicates that a view is docked to the side.
         */
        DOCKED,

        /**
         * Indicates that the view is registered but neither docked or viewed.
         */
        HIDDEN,
    }
}
