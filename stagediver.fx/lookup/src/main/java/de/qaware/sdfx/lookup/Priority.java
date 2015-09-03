/*
 * #%L
 * The core lookup module of the stagediver.fx platform and all applications.
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
package de.qaware.sdfx.lookup;

import java.lang.annotation.*;

/**
 * Defines the priority within the returned list of {@link LookupStrategy#lookupAll(Class)}.
 * <p>
 * The higher the more important. Default value is 0.
 *
 * @author christian.fritz
 * @deprecated Please use {@link javax.annotation.Priority} instead
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
@Deprecated
public @interface Priority {

    /**
     * The priority.
     */
    int value() default 0;
}
