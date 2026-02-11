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
package de.weigend.wfx.extensions.cdi.contexts;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXMLLoader;

import java.nio.charset.StandardCharsets;

/**
 * Produces the FXMLLoader when using the cdi module.
 *
 * @author Software-EKG Team
 */
@Singleton
public class FXMLLoaderProducer {

    @Inject
    private Instance<Object> instance;

    /**
     * Producer method to create new instances of the {@link javafx.fxml.FXMLLoader} which initializes the controllers
     * with cdi.
     *
     * @return The cdi specific fxml loader.
     */
    @Produces
    @Dependent
    public FXMLLoader createLoader() {
        return new FXMLLoader(null, null, null, this::controllerFactory, StandardCharsets.UTF_8);
    }

    /**
     * The controller factory to create the controller instances. It uses CDI to create the real instances but will
     * never return a proxy instance.
     *
     * @param controllerClass The requested controller type class.
     * @param <T>             The type of the requested instance.
     * @return The created instance. Possible proxies are unpacked.
     */
    private <T> T controllerFactory(Class<T> controllerClass) {
        T controller = instance.select(controllerClass).get();
        return BeanUtils.getUnwrappedInstance(controller);
    }
}
