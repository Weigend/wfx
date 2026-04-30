/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
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
package io.softwareecg.wfx.lookup.avaje.testbeans;

import io.avaje.inject.Secondary;
import jakarta.inject.Singleton;

/**
 * Plays the role of a WFX-shipped fallback bean for
 * {@link OverridableService}. Marked {@link Secondary} so any consumer-
 * registered {@code @Singleton OverridableService} overrides it via
 * Avaje DI without an explicit {@code @Primary} on the override.
 */
@Secondary
@Singleton
public class A_SecondaryService implements OverridableService {

    @Override
    public String identify() {
        return "secondary-default";
    }
}
