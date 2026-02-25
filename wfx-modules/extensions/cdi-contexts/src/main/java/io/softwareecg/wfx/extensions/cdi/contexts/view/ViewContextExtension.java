/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 Weigend AM
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
package io.softwareecg.wfx.extensions.cdi.contexts.view;

import io.softwareecg.wfx.extensions.cdi.contexts.api.ViewContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Singleton;
import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;

/**
 * Register the {@link ViewContext} within the current cdi instance.
 *
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
        setViewContextContext(new ViewContextImpl(RegistrySingletonProvider.STATIC_INSTANCE));
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
