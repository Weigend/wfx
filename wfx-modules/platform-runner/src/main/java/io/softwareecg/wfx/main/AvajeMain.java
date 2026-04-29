/*
 * #%L
 * The platform-runner module is main start module for the wfx platform.
 * %%
 * Copyright (C) 2013 - 2026 Weigend AM
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
package io.softwareecg.wfx.main;

import io.softwareecg.wfx.lookup.avaje.AvajeLookupStrategy;

/**
 * wfx application startup class with Avaje Inject as lookup strategy.
 */
public class AvajeMain extends Main {

    @Override
    public void init() {
        AvajeLookupStrategy.initLookup();
        super.init();
    }

    /**
     * Shutdown the Avaje BeanScope when the application stops.
     * Ensures proper cleanup of all managed beans (including {@code @Bean}
     * destroyMethod hooks like {@code CoreContainer.shutdown()}).
     *
     * @throws Exception In case of any errors while stopping the application.
     */
    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void stop() throws Exception {
        super.stop();
        AvajeLookupStrategy.shutdownLookup();
    }
}
