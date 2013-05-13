package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;

/**
 * Stores the current status and additional metadata of an window manager view.
 */
public final class ViewStatus {

    /**
     * The status whether this view is visible or hidden (or something else)
     */
    private Status status;

    /**
     * The registered view
     */
    private final View view;

    /**
     * The current position within the window
     */
    private Position position;

    private final Position defaultPosition;

    private final ViewStatus parent;

    private TabArea area;

    /**
     * The tab which contains this view.
     */
    private Tab tab;

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
        this.setStatus(Status.VISIBLE);
        this.parent = parent;
        this.initTab();
    }

    public ViewStatus getParent() {
        return parent;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public View getView() {
        return view;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

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
        tab.setTooltip(new Tooltip(view.getToolTipInfo()));
        tab.setUserData(this);

        tab.setOnClosed(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                ViewStatus status = ViewStatus.this;
                status.getArea().remove(status);
                status.setStatus(Status.HIDDEN);
            }
        });
    }

    public TabArea getArea() {
        return area;
    }

    public void setArea(TabArea area) {
        this.area = area;
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
