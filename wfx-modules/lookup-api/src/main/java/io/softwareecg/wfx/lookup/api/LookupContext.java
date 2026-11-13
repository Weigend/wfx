/*
 * #%L
 * The core lookup module of the wfx platform and all applications.
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
package io.softwareecg.wfx.lookup.api;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A lookup that belongs to one place and whose content changes over time.
 * <p>
 * {@link Lookup} answers "which service implements this interface?" for the
 * whole application, and the answer is fixed once the application has
 * started. A {@code LookupContext} answers "what is this view currently
 * about?": the selected project, the open document, the capabilities the
 * current selection offers. Its owner puts objects in and takes them out as
 * the user works, and anyone interested asks for them by type — and can
 * {@linkplain #observe(Class, Consumer) observe} the answer changing. The type
 * is the only contract between the two sides.
 * <p>
 * A view offers its context through {@code View.getLookup()}; the window
 * manager offers the context of the view the user is working in through
 * {@code WindowManager.getActiveLookup()}. An action that should be enabled
 * only while a project is selected observes {@code Project.class} there and
 * never needs to know which view the selection comes from.
 * <p>
 * Implementations: {@link MutableLookupContext} to hold content,
 * {@link ProxyLookupContext} to stand in for a context that gets exchanged.
 */
public interface LookupContext {

    /**
     * A context that never contains anything.
     */
    LookupContext EMPTY = new LookupContext() {
        @Override
        public <T> List<T> lookupAll(Class<T> type) {
            return List.of();
        }

        @Override
        public <T> Subscription observe(Class<T> type, Consumer<? super List<T>> listener) {
            return () -> { };
        }

        @Override
        public String toString() {
            return "LookupContext.EMPTY";
        }
    };

    /**
     * Get all objects of the given type.
     *
     * @param type the type to look for
     * @param <T>  the type to look for
     * @return the instances of the type in the order they were put into the
     *         context; an empty list if there are none, never null. The list
     *         is a snapshot and does not change.
     */
    <T> List<T> lookupAll(Class<T> type);

    /**
     * Get the first object of the given type.
     *
     * @param type the type to look for
     * @param <T>  the type to look for
     * @return the first element of {@link #lookupAll(Class)}, or null if
     *         there is none
     */
    default <T> T lookup(Class<T> type) {
        List<T> all = lookupAll(type);
        return all.isEmpty() ? null : all.get(0);
    }

    /**
     * Get the first object of the given type as an {@link Optional}.
     *
     * @param type the type to look for
     * @param <T>  the type to look for
     * @return the first element of {@link #lookupAll(Class)}, if any
     */
    default <T> Optional<T> find(Class<T> type) {
        return Optional.ofNullable(lookup(type));
    }

    /**
     * Get told when the objects of the given type change.
     * <p>
     * The listener is called with the new result of {@link #lookupAll(Class)}
     * whenever that result differs from the previous one. It is not called
     * for changes that concern other types only, and not for the state at the
     * time of the call — ask {@link #lookupAll(Class)} for that. It runs on
     * the thread that changed the content.
     * <p>
     * The context keeps the listener, and everything the listener refers to,
     * alive until the returned subscription is cancelled.
     *
     * @param type     the type to watch
     * @param listener receives the current instances of the type after a change
     * @param <T>      the type to watch
     * @return the handle to end the observation with
     */
    <T> Subscription observe(Class<T> type, Consumer<? super List<T>> listener);

    /**
     * Something that has a context to offer, for example a view or the
     * controller behind it.
     */
    interface Provider {

        /**
         * Get the context of this object.
         *
         * @return the context, never null; always the same instance
         */
        LookupContext getLookup();
    }

    /**
     * An ongoing {@linkplain LookupContext#observe(Class, Consumer) observation}.
     */
    @FunctionalInterface
    interface Subscription {

        /**
         * End the observation. The listener is not called any more and is no
         * longer referenced by the context. Cancelling twice is harmless.
         */
        void cancel();
    }
}
