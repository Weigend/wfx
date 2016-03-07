package de.qaware.sdfx.extensions.cdi.contexts.testviews;

import de.qaware.sdfx.extensions.cdi.contexts.api.ViewScoped;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import javafx.scene.Parent;

/**
 * A test view implementation that is {@link ViewScoped}.
 *
 * @author christian.fritz
 */
@ViewScoped
public class ViewScopedView implements View {
    @Override
    public String getViewId() {
        return getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return getViewId();
    }

    @Override
    public String getToolTipInfo() {
        return getViewId();
    }

    @Override
    public Position getDefaultPosition() {
        return null;
    }

    @Override
    public Parent getRootNode() {
        return null;
    }

    @Override
    public double getViewAreaSize() {
        return 0;
    }
}
