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
package de.weigend.wfx.extensions.cdi.contexts;

import de.weigend.wfx.extensions.cdi.contexts.api.Eager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.*;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

/**
 * CDI extension to eager initialize beans annotated with {@link Eager}.
 *
 */
public class EagerExtension implements Extension {
    private final List<Bean<?>> eagerBeansList = new ArrayList<>();

    /**
     * Collect the beans to initialize
     *
     * @param event the bean to initialize
     * @param <T>   the type of the bean
     */
    public <T> void collect(@Observes ProcessBean<T> event) {
        Annotated annotated = event.getAnnotated();
        if (annotated.isAnnotationPresent(Eager.class) && isSingleton(event)) {
            eagerBeansList.add(event.getBean());
        }
    }

    /**
     * Initialize the collected beans.
     *
     * @param event       observes the after deployment validation.
     * @param beanManager the bean manager.
     */
    public void load(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
        eagerBeansList.forEach(bean ->
                beanManager.getReference(bean, bean.getBeanClass(), beanManager.createCreationalContext(bean)).toString()
        );
    }

    /**
     * Check if the bean is a singleton.
     *
     * @param bean the bean to check
     * @return true if the bean is a singleton, otherwise false.
     */
    private static boolean isSingleton(ProcessBean<?> bean) {
        Annotated annotated = bean.getAnnotated();
        return annotated.isAnnotationPresent(ApplicationScoped.class) || annotated.isAnnotationPresent(Singleton.class);
    }
}