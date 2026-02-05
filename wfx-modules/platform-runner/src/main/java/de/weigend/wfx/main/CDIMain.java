/*
 * #%L
 * The platform-runner module is main start module for the stagediver.fx platform.
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
package de.weigend.wfx.main;

import de.weigend.wfx.lookup.cdi.CDILookupStrategy;

/**
 * stagediver.fx application startup class with cdi as lookup strategy.
 *
 * @author christian.fritz
 */
public class CDIMain extends Main {
    @Override
    public void init() {
        CDILookupStrategy.initLookup();
        super.init();
    }

    /**
     * Shutdown the CDI container when the application stops.
     * This ensures proper cleanup of all managed beans and resources.
     *
     * @throws Exception In case of any errors while stopping the application.
     */
    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public void stop() throws Exception {
        super.stop();
        CDILookupStrategy.shutdownLookup();
    }
}
