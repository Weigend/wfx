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
package io.softwareecg.wfx.lookup;

import io.softwareecg.wfx.lookup.api.LookupContext;
import io.softwareecg.wfx.lookup.api.MutableLookupContext;
import io.softwareecg.wfx.lookup.api.ProxyLookupContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

/**
 * Unit test for the context lookups: {@link MutableLookupContext},
 * {@link ProxyLookupContext} and {@link LookupContext#EMPTY}.
 */
public class LookupContextTest {

    /** Records what an observer was told. */
    private static final class Recorder<T> {
        private final List<List<T>> calls = new ArrayList<>();

        void accept(List<T> now) {
            calls.add(now);
        }

        List<T> last() {
            return calls.get(calls.size() - 1);
        }
    }

    // ---------------------------------------------------------------- EMPTY

    @Test
    public void emptyContextContainsNothingAndNeverNotifies() {
        Recorder<String> recorder = new Recorder<>();
        LookupContext.Subscription subscription = LookupContext.EMPTY.observe(String.class, recorder::accept);

        assertThat(LookupContext.EMPTY.lookupAll(Object.class), is(empty()));
        assertThat(LookupContext.EMPTY.lookup(String.class), is(nullValue()));
        assertThat(LookupContext.EMPTY.find(String.class).isPresent(), is(false));
        subscription.cancel();
        assertThat(recorder.calls, is(empty()));
    }

    // -------------------------------------------------------------- mutable

    @Test
    public void lookupIsByTypeInInsertionOrder() {
        MutableLookupContext context = new MutableLookupContext("b", 1, "a", 2L);

        assertThat(context.lookupAll(String.class), contains("b", "a"));
        assertThat(context.lookupAll(Number.class), contains((Number) 1, 2L));
        assertThat(context.lookup(String.class), is("b"));
        assertThat(context.find(Long.class).orElseThrow(), is(2L));
        assertThat(context.lookup(Runnable.class), is(nullValue()));
    }

    @Test
    public void anObjectIsContainedAtMostOnce() {
        MutableLookupContext context = new MutableLookupContext();

        assertThat(context.add("a"), is(true));
        assertThat(context.add("a"), is(false));
        assertThat(context.lookupAll(String.class), hasSize(1));
        assertThat(context.remove("a"), is(true));
        assertThat(context.remove("a"), is(false));
    }

    @Test
    public void lookupAllReturnsASnapshot() {
        MutableLookupContext context = new MutableLookupContext("a");
        List<String> before = context.lookupAll(String.class);

        context.add("b");

        assertThat(before, contains("a"));
    }

    @Test
    public void observerIsToldTheNewInstancesOfItsType() {
        MutableLookupContext context = new MutableLookupContext();
        Recorder<String> recorder = new Recorder<>();
        context.observe(String.class, recorder::accept);

        context.add("a");
        context.add("b");
        context.remove("a");

        assertThat(recorder.calls, hasSize(3));
        assertThat(recorder.calls.get(0), contains("a"));
        assertThat(recorder.calls.get(1), contains("a", "b"));
        assertThat(recorder.calls.get(2), contains("b"));
    }

    @Test
    public void observerIsNotToldAboutOtherTypes() {
        MutableLookupContext context = new MutableLookupContext("a");
        Recorder<String> recorder = new Recorder<>();
        context.observe(String.class, recorder::accept);

        context.add(42);
        context.remove(42);

        assertThat(recorder.calls, is(empty()));
    }

    @Test
    public void observerIsNotToldWhenNothingChanges() {
        MutableLookupContext context = new MutableLookupContext("a");
        Recorder<String> recorder = new Recorder<>();
        context.observe(String.class, recorder::accept);

        context.add("a");
        context.remove("not there");
        context.set(List.of("a"));

        assertThat(recorder.calls, is(empty()));
    }

    @Test
    public void setReplacesTheContentWithOneNotification() {
        MutableLookupContext context = new MutableLookupContext("a", "b", 1);
        Recorder<String> strings = new Recorder<>();
        Recorder<Integer> integers = new Recorder<>();
        context.observe(String.class, strings::accept);
        context.observe(Integer.class, integers::accept);

        context.set(List.of("c", 1));

        assertThat(strings.calls, hasSize(1));
        assertThat(strings.last(), contains("c"));
        assertThat("the integers did not change", integers.calls, is(empty()));
        assertThat(context.lookupAll(Object.class), contains((Object) "c", 1));
    }

    @Test
    public void clearEmptiesTheContext() {
        MutableLookupContext context = new MutableLookupContext("a");
        Recorder<String> recorder = new Recorder<>();
        context.observe(String.class, recorder::accept);

        context.clear();

        assertThat(context.lookupAll(Object.class), is(empty()));
        assertThat(recorder.last(), is(empty()));
    }

    @Test
    public void cancelledObserverIsNotToldAnyMore() {
        MutableLookupContext context = new MutableLookupContext();
        Recorder<String> recorder = new Recorder<>();
        LookupContext.Subscription subscription = context.observe(String.class, recorder::accept);

        subscription.cancel();
        subscription.cancel();
        context.add("a");

        assertThat(recorder.calls, is(empty()));
    }

    @Test
    public void sameListenerObservingTwiceIsCancelledSeparately() {
        MutableLookupContext context = new MutableLookupContext();
        Recorder<String> recorder = new Recorder<>();
        java.util.function.Consumer<List<String>> listener = recorder::accept;
        LookupContext.Subscription first = context.observe(String.class, listener);
        context.observe(String.class, listener);

        first.cancel();
        context.add("a");

        assertThat(recorder.calls, hasSize(1));
    }

    @Test
    public void listenerMayChangeTheContext() {
        MutableLookupContext context = new MutableLookupContext();
        context.observe(String.class, strings -> {
            if (strings.contains("ping") && !strings.contains("pong")) {
                context.add("pong");
            }
        });

        context.add("ping");

        assertThat(context.lookupAll(String.class), contains("ping", "pong"));
    }

    @Test(expected = NullPointerException.class)
    public void nullIsNotAcceptedAsContent() {
        new MutableLookupContext().add(null);
    }

    // ---------------------------------------------------------------- proxy

    @Test
    public void proxyAnswersFromItsDelegate() {
        ProxyLookupContext proxy = new ProxyLookupContext();
        assertThat(proxy.lookupAll(String.class), is(empty()));
        assertThat(proxy.getDelegate(), is(sameInstance(LookupContext.EMPTY)));

        proxy.setDelegate(new MutableLookupContext("a"));

        assertThat(proxy.lookup(String.class), is("a"));
    }

    @Test
    public void proxyObserverIsToldWhenTheDelegateContentChanges() {
        MutableLookupContext delegate = new MutableLookupContext();
        ProxyLookupContext proxy = new ProxyLookupContext();
        proxy.setDelegate(delegate);
        Recorder<String> recorder = new Recorder<>();
        proxy.observe(String.class, recorder::accept);

        delegate.add("a");

        assertThat(recorder.last(), contains("a"));
    }

    @Test
    public void proxyObserverIsToldWhenExchangingTheDelegateChangesTheAnswer() {
        ProxyLookupContext proxy = new ProxyLookupContext();
        proxy.setDelegate(new MutableLookupContext("a"));
        Recorder<String> recorder = new Recorder<>();
        proxy.observe(String.class, recorder::accept);

        proxy.setDelegate(new MutableLookupContext("b"));
        assertThat(recorder.last(), contains("b"));

        proxy.setDelegate(null);
        assertThat(recorder.last(), is(empty()));
        assertThat(recorder.calls, hasSize(2));
    }

    @Test
    public void proxyObserverIsNotToldWhenTheNewDelegateAnswersTheSame() {
        ProxyLookupContext proxy = new ProxyLookupContext();
        proxy.setDelegate(new MutableLookupContext("a", 1));
        Recorder<String> recorder = new Recorder<>();
        proxy.observe(String.class, recorder::accept);

        proxy.setDelegate(new MutableLookupContext("a", 2));

        assertThat(recorder.calls, is(empty()));
    }

    @Test
    public void proxyStopsListeningToAFormerDelegate() {
        MutableLookupContext former = new MutableLookupContext();
        MutableLookupContext current = new MutableLookupContext();
        ProxyLookupContext proxy = new ProxyLookupContext();
        proxy.setDelegate(former);
        Recorder<String> recorder = new Recorder<>();
        proxy.observe(String.class, recorder::accept);
        proxy.setDelegate(current);

        former.add("stale");

        assertThat(recorder.calls, is(empty()));
        current.add("fresh");
        assertThat(recorder.last(), contains("fresh"));
    }

    @Test
    public void cancelledProxyObserverIsNotToldAnyMore() {
        MutableLookupContext delegate = new MutableLookupContext();
        ProxyLookupContext proxy = new ProxyLookupContext();
        proxy.setDelegate(delegate);
        Recorder<String> recorder = new Recorder<>();
        LookupContext.Subscription subscription = proxy.observe(String.class, recorder::accept);

        subscription.cancel();
        subscription.cancel();
        delegate.add("a");
        proxy.setDelegate(new MutableLookupContext("b"));

        assertThat(recorder.calls, is(empty()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void proxyCannotStandInForItself() {
        ProxyLookupContext proxy = new ProxyLookupContext();
        proxy.setDelegate(proxy);
    }
}
