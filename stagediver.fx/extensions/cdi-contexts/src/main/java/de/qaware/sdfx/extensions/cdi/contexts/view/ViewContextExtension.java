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
package de.qaware.sdfx.extensions.cdi.contexts.view;

import de.qaware.sdfx.extensions.cdi.contexts.api.ViewContext;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Singleton;

/**
 * Register the {@link ViewContext} within the current cdi instance.
 *
 * @author christian.fritz
 */
public class ViewContextExtension implements Extension {

    private static ViewContext viewContextContext;

    private static void setViewContextContext(ViewContext viewContextContext) {
        ViewContextExtension.viewContextContext = viewContextContext;
    }

    /**
     * Wait for {@link AfterBeanDiscovery AfterBeanDiscovery event} and initialize the {@link ViewContext}.
     *
     * @param event The {@link AfterBeanDiscovery event} to listen for.
     */
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event) {
        setViewContextContext(new ViewContextImpl("STATIC_INSTANCE"));
        event.addContext(viewContextContext);
    }

    /**
     * Producer to inject the current context instance.
     */
    @Singleton
    @SuppressWarnings("unused")
    public static class Producer {

        /**
         * Context producer to inject the current context.
         *
         * @return The context.
         */
        @Produces
        @Singleton
        public ViewContext produceContext() {
            return viewContextContext;
        }
    }
}
