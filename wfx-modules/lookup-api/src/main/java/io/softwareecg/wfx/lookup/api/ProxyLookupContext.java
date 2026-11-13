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
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@link LookupContext} that stands in for another one, which can be
 * exchanged.
 * <p>
 * Whoever holds the proxy keeps a stable reference and stable observations
 * while the context behind it changes — this is how
 * {@code WindowManager.getActiveLookup()} follows the user from view to view.
 * An observer is told when the content of the current delegate changes, and
 * also when exchanging the delegate changes what it sees.
 * <p>
 * The proxy is safe to use from several threads.
 */
public final class ProxyLookupContext implements LookupContext {

    private final Object lock = new Object();
    private LookupContext delegate = EMPTY;
    private final List<Observer<?>> observers = new ArrayList<>();

    /**
     * Get the context this proxy currently stands in for.
     *
     * @return the delegate, {@link LookupContext#EMPTY} if none was set
     */
    public LookupContext getDelegate() {
        synchronized (lock) {
            return delegate;
        }
    }

    /**
     * Exchange the context this proxy stands in for.
     *
     * @param newDelegate the context to answer from now on; null means
     *                    {@link LookupContext#EMPTY}
     */
    public void setDelegate(LookupContext newDelegate) {
        LookupContext next = newDelegate != null ? newDelegate : EMPTY;
        if (next == this) {
            throw new IllegalArgumentException("A proxy cannot stand in for itself");
        }
        List<Runnable> notifications = new ArrayList<>();
        synchronized (lock) {
            if (next == delegate) {
                return;
            }
            delegate = next;
            for (Observer<?> observer : observers) {
                observer.switchTo(next, notifications);
            }
        }
        notifications.forEach(Runnable::run);
    }

    @Override
    public <T> List<T> lookupAll(Class<T> type) {
        return getDelegate().lookupAll(type);
    }

    @Override
    public <T> Subscription observe(Class<T> type, Consumer<? super List<T>> listener) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(listener, "listener");
        Observer<T> observer = new Observer<>(type, listener);
        synchronized (lock) {
            observer.attach(delegate);
            observers.add(observer);
        }
        return () -> {
            synchronized (lock) {
                if (observers.remove(observer)) {
                    observer.detach();
                }
            }
        };
    }

    @Override
    public String toString() {
        return "ProxyLookupContext -> " + getDelegate();
    }

    /**
     * One observation of the proxy, re-attached to each new delegate.
     */
    private static final class Observer<T> {
        private final Class<T> type;
        private final Consumer<? super List<T>> listener;
        private Subscription upstream;
        private volatile List<T> lastSeen;

        Observer(Class<T> type, Consumer<? super List<T>> listener) {
            this.type = type;
            this.listener = listener;
        }

        void attach(LookupContext context) {
            lastSeen = context.lookupAll(type);
            upstream = context.observe(type, now -> {
                lastSeen = List.copyOf(now);
                listener.accept(now);
            });
        }

        void detach() {
            if (upstream != null) {
                upstream.cancel();
                upstream = null;
            }
        }

        void switchTo(LookupContext context, List<Runnable> notifications) {
            List<T> before = lastSeen;
            detach();
            attach(context);
            List<T> now = lastSeen;
            if (!now.equals(before)) {
                notifications.add(() -> listener.accept(now));
            }
        }
    }
}
