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
package io.softwareecg.wfx.extensions.cdi.contexts;

import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

/**
 * Utilities to work with cdi beans.
 *
 */
public final class BeanUtils {

    private BeanUtils() {
    }

    /**
     * Check if the given bean is proxied.
     *
     * @param object the bean to check
     * @return true if the bean is proxied by cdi, otherwise false
     */
    public static boolean isProxied(Object object) {
        return object instanceof TargetInstanceProxy;
    }

    /**
     * Get the unwrapped bean for the current contexts.
     *
     * @param object the bean to unwrap.
     * @param <T>    The type of the bean to unwrap.
     * @return the unwrapped bean if it is proxied, otherwise {@code object}
     */
    @SuppressWarnings("unchecked")
    public static <T> T getUnwrappedInstance(T object) {
        if (isProxied(object)) {
            return ((TargetInstanceProxy<T>) object).weld_getTargetInstance();
        }
        return object;
    }
}
