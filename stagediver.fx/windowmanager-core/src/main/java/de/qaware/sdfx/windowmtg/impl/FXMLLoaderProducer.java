/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
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
package de.qaware.sdfx.windowmtg.impl;

import javafx.fxml.*;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;

/**
 * Produces the FXMLLoader when using the cdi module.
 *
 * @author christian.fritz
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
    public FXMLLoader createLoader() {
        return new FXMLLoader(null, null, null, controllerClass -> instance.select(controllerClass).get(), StandardCharsets.UTF_8);
    }
}
