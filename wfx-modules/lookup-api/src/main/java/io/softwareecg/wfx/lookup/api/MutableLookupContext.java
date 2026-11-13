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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * A {@link LookupContext} whose owner decides what is in it.
 * <p>
 * Typical use by a view controller:
 * <pre>{@code
 * private final MutableLookupContext lookup = new MutableLookupContext();
 *
 * tree.getSelectionModel().selectedItemProperty().addListener((o, old, item) ->
 *         lookup.set(item == null ? List.of() : List.of(item.getValue())));
 * }</pre>
 * An object is contained at most once. The context is safe to use from
 * several threads; listeners run on the thread that made the change.
 */
public final class MutableLookupContext implements LookupContext {

    private final Object lock = new Object();
    /** Replaced, never modified in place, so readers need no lock. */
    private volatile List<Object> content = List.of();
    private final List<Observer<?>> observers = new CopyOnWriteArrayList<>();

    /**
     * Create an empty context.
     */
    public MutableLookupContext() {
    }

    /**
     * Create a context with initial content.
     *
     * @param initialContent the objects to start with
     */
    public MutableLookupContext(Object... initialContent) {
        set(Arrays.asList(initialContent));
    }

    /**
     * Put an object into the context.
     *
     * @param instance the object to add; must not be null
     * @return false if the context already contained it
     */
    public boolean add(Object instance) {
        Objects.requireNonNull(instance, "instance");
        return change(next -> next.add(instance));
    }

    /**
     * Take an object out of the context.
     *
     * @param instance the object to remove
     * @return false if the context did not contain it
     */
    public boolean remove(Object instance) {
        return change(next -> next.remove(instance));
    }

    /**
     * Replace the whole content, for example with the new selection.
     * Observers are told once, and only those whose type is affected.
     *
     * @param newContent the objects the context should contain from now on,
     *                   in this order; must not contain null
     */
    public void set(Collection<?> newContent) {
        for (Object instance : newContent) {
            Objects.requireNonNull(instance, "content must not contain null");
        }
        change(next -> {
            if (new ArrayList<>(next).equals(new ArrayList<>(new LinkedHashSet<>(newContent)))) {
                return false;
            }
            next.clear();
            next.addAll(newContent);
            return true;
        });
    }

    /**
     * Remove everything.
     */
    public void clear() {
        set(List.of());
    }

    @Override
    public <T> List<T> lookupAll(Class<T> type) {
        return filter(content, type);
    }

    @Override
    public <T> Subscription observe(Class<T> type, Consumer<? super List<T>> listener) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(listener, "listener");
        Observer<T> observer = new Observer<>(type, listener);
        observers.add(observer);
        return () -> observers.remove(observer);
    }

    /**
     * Apply a change to a copy of the content and, if it took effect, publish
     * the copy and tell the observers whose view of the content differs now.
     */
    private boolean change(java.util.function.Predicate<LinkedHashSet<Object>> mutation) {
        List<Runnable> notifications = new ArrayList<>();
        synchronized (lock) {
            List<Object> before = content;
            LinkedHashSet<Object> next = new LinkedHashSet<>(before);
            if (!mutation.test(next)) {
                return false;
            }
            List<Object> after = List.copyOf(next);
            content = after;
            for (Observer<?> observer : observers) {
                observer.collect(before, after, notifications);
            }
        }
        // Outside the lock: a listener may well change the context again.
        notifications.forEach(Runnable::run);
        return true;
    }

    private static <T> List<T> filter(List<Object> all, Class<T> type) {
        List<T> matching = new ArrayList<>();
        for (Object instance : all) {
            if (type.isInstance(instance)) {
                matching.add(type.cast(instance));
            }
        }
        return List.copyOf(matching);
    }

    @Override
    public String toString() {
        return "MutableLookupContext" + content;
    }

    private record Observer<T>(Class<T> type, Consumer<? super List<T>> listener) {

        void collect(List<Object> before, List<Object> after, List<Runnable> notifications) {
            List<T> now = filter(after, type);
            if (!now.equals(filter(before, type))) {
                notifications.add(() -> listener.accept(now));
            }
        }

        // Identity, not record equality: the same listener may observe twice
        // and each subscription has to be cancellable on its own.
        @Override
        public boolean equals(Object other) {
            return this == other;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }
}
