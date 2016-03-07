package de.qaware.sdfx.extensions.cdi.contexts;

import de.qaware.sdfx.extensions.cdi.contexts.api.JfxContext;
import de.qaware.sdfx.windowmtg.api.View;
import de.qaware.sdfx.windowmtg.api.WindowManager;

import javax.annotation.PostConstruct;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Handles the correct context activation when the focus switches to a different view.
 *
 * @author christian.fritz
 */
@Singleton
public class ScopeHandler {

    @Inject
    private BeanManager beanManager;
    @Inject
    private WindowManager windowManager;

    /**
     * Register the change listener when the focus switches.
     */
    @PostConstruct
    public void initScopeHandler() {
        windowManager.focusedViewProperty().addListener((o, ov, nv) -> focusedViewChangeListener(nv));
    }

    /**
     * The listener executed when the focus view changes.
     *
     * @param nv the new focused view.
     */
    @SuppressWarnings("unchecked")
    private void focusedViewChangeListener(View nv) {
        Annotation scopeAnnotation = findScopeAnnotation(nv.getClass());
        if (scopeAnnotation == null) {
            return;
        }
        Context c = beanManager.getContext(scopeAnnotation.annotationType());
        if (!(c instanceof JfxContext)) {
            return;
        }
        JfxContext context = (JfxContext) c;
        Object contextStorage = context.getStorageIdentifierFor(nv);
        if (!Objects.equals(context.getAssociatedStorage(), contextStorage)) {
            context.associate(contextStorage, true);
        }
    }

    /**
     * Find the {@link javax.inject.Scope} annotation at the given view.
     *
     * @param clazz The class to check
     * @return The found annotation or null if no {@link javax.inject.Scope} annotation was found.
     */
    private Annotation findScopeAnnotation(Class clazz) {
        Annotation scopeAnnotation = null;
        for (Annotation annotation : clazz.getAnnotations()) {
            if (beanManager.isScope(annotation.annotationType())) {
                scopeAnnotation = annotation;
                break;
            }
        }
        return scopeAnnotation;
    }
}
