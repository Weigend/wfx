package de.qaware.sdfx.extensions.cdi.contexts.view;

import de.qaware.sdfx.extensions.cdi.contexts.AbstractJfxContext;
import de.qaware.sdfx.extensions.cdi.contexts.api.ViewContext;
import de.qaware.sdfx.extensions.cdi.contexts.api.ViewScoped;

import java.lang.annotation.Annotation;

/**
 * Implementation for the {@link ViewContext}.
 *
 * @author christian.fritz
 */
public class ViewContextImpl extends AbstractJfxContext<String> implements ViewContext {
    /**
     * Initialize a new view context.
     *
     * @param contextId the context id.
     */
    protected ViewContextImpl(String contextId) {
        super(contextId, "ViewScopedContext");
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ViewScoped.class;
    }
}
