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

package de.qaware.sdfx.lookup.cdi;

import de.qaware.sdfx.lookup.Lookup;

/**
 * Some helper utils to start and shutdown the stagediver.fx cdi container while testing.
 *
 * @author christian.fritz
 */
public class CdiLookupTestHelper {

    /**
     * Start the cdi container and initialize the lookup.
     */
    public static void initCdiLookup() {
        CDILookupStrategy.initLookup();
    }

    /**
     * Shutdown the cdi container.
     */
    public static void shutDownCdiLookup() {
        if (CDILookupStrategy.getShutdownHook() != null) {
            Runtime.getRuntime().removeShutdownHook(CDILookupStrategy.getShutdownHook());
        }
        CDILookupStrategy.setShutdownHook(null);
        if (CDILookupStrategy.getWeld() != null) {
            CDILookupStrategy.getWeld().shutdown();
            CDILookupStrategy.setWeld(null);
        }
        Lookup.init(null);
    }
}
