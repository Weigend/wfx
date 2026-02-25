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
package io.softwareecg.wfx.extensions.cdi.contexts.api;

import org.jboss.weld.context.BoundContext;
import org.jboss.weld.context.ManagedContext;

import java.util.Set;

/**
 * The JfxContext defines the context in which several views can be combined.
 * <p/>
 * For example a search can be split into two separate views but using the same backend model.
 * <p/>
 * The {@link io.softwareecg.wfx.extensions.cdi.contexts.api.ViewContext} with the implementation {@link
 * io.softwareecg.wfx.extensions.cdi.contexts.view.ViewContextImpl} is an example how such a context can be implemented. All
 * views annotated with {@link ViewScoped} can use the same backend model. But two views of the same type may use
 * different backend models if they are created for different storages.
 *
 * @param <T> The type of the associated storage
 */
public interface JfxContext<T> extends BoundContext<T>, ManagedContext {
    /**
     * Associate the context with the storage (for this thread). If {@code dissociateExisting} is true, a possible
     * associated context will be dissociated. Otherwise the context won't be associated and it returns false.
     *
     * @param storage            the external storage
     * @param dissociateExisting dissociate a possible pre associated context
     * @return true if the storage was attached, otherwise false
     */
    boolean associate(T storage, boolean dissociateExisting);

    /**
     * Get the current associated storage (for this thread). If no storage is associated it returns null.
     *
     * @return The associated storage or null.
     */
    T getAssociatedStorage();

    /**
     * Get the current valid bean store identifier.
     *
     * @return A set with the valid bean store identifier.
     */
    Set<T> getStorageIdentifier();

    /**
     * Get the store identifier for the given object.
     *
     * @param obj The object to retrieve the storage identifier.
     * @return The found storage identifier or null if the object is not managed by this context.
     */
    T getStorageIdentifierFor(Object obj);
}
