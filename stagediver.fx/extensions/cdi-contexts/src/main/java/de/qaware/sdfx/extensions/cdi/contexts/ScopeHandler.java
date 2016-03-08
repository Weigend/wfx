/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 QAware GmbH
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
package de.qaware.sdfx.extensions.cdi.contexts;

import de.qaware.sdfx.extensions.cdi.contexts.api.Eager;
import de.qaware.sdfx.extensions.cdi.contexts.api.JfxContext;
import de.qaware.sdfx.windowmtg.api.FXMLView;
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
@Eager
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
     * @param view the new focused view.
     */
    @SuppressWarnings("unchecked")
    private void focusedViewChangeListener(View view) {
        Annotation scopeAnnotation = findScopeAnnotation(findClass(view));
        if (scopeAnnotation == null) {
            return;
        }
        Context c = beanManager.getContext(scopeAnnotation.annotationType());
        if (!(c instanceof JfxContext)) {
            return;
        }
        JfxContext context = (JfxContext) c;
        Object contextStorage = context.getStorageIdentifierFor(view);
        if (contextStorage != null && !Objects.equals(context.getAssociatedStorage(), contextStorage)) {
            context.associate(contextStorage, true);
        }
    }

    /**
     * Find the real class for scope detection.
     *
     * @param view view for detection
     * @return the class for scope detection
     */
    private static Class findClass(View view) {
        if (view instanceof FXMLView) {
            return ((FXMLView) view).getController().getClass();
        }
        return view.getClass();
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
