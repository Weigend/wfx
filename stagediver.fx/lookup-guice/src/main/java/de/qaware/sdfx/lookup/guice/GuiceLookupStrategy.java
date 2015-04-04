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
package de.qaware.sdfx.lookup.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import de.qaware.sdfx.lookup.LookupStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses google guice to lookup instances.
 *
 * @author christian.fritz
 */
public class GuiceLookupStrategy implements LookupStrategy {

    private Injector injector;

    /**
     * Init the lookup if the module is not running within an osgi container.
     *
     * @param modules A list of guice modules.
     */
    public GuiceLookupStrategy(Module... modules) {
        injector = Guice.createInjector(modules);
    }

    /**
     * Init the lookup if the module is not running within an osgi container.
     *
     * @param modules A list of guice modules.
     */
    public GuiceLookupStrategy(Iterable<Module> modules) {
        injector = Guice.createInjector(modules);
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    @Override
    public <T> List<T> lookupAll(Class<T> clazz) {
        List<T> services = new ArrayList<>();
        services.add(injector.getInstance(clazz));
        return services;
    }
}
