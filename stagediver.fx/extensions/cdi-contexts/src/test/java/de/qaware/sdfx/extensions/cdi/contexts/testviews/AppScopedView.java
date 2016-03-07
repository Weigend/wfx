package de.qaware.sdfx.extensions.cdi.contexts.testviews;

import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import javafx.scene.Parent;

import javax.enterprise.context.ApplicationScoped;

/**
 * A test view implementation that is {@link ApplicationScoped}.
 *
 * @author christian.fritz
 */
@ApplicationScoped
public class AppScopedView implements View {
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
        return null;
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
