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

import io.softwareecg.wfx.lookup.api.Priorities;
import jakarta.annotation.Priority;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * Unit test for {@link Priorities}, the single definition of how WFX reads
 * {@code @Priority}.
 */
public class PrioritiesTest {

    @Test
    public void annotatedClassHasItsDeclaredPriority() {
        assertThat(Priorities.of(Early.class), is(10));
        assertThat(Priorities.of(new Late()), is(1000));
    }

    @Test
    public void unannotatedClassHasTheDefaultPriority() {
        assertThat(Priorities.of(Plain.class), is(Priorities.DEFAULT));
    }

    @Test
    public void smallerValueComesFirstAndUnannotatedLast() {
        Object early = new Early();
        Object late = new Late();
        Object plain = new Plain();
        Object negative = new Negative();
        List<Object> candidates = new ArrayList<>(List.of(plain, late, negative, early));

        candidates.sort(Priorities.comparator());

        assertThat(candidates, contains(negative, early, late, plain));
    }

    @Test
    public void equalPrioritiesKeepTheirOrder() {
        Object first = new Plain();
        Object second = new Plain();
        List<Object> candidates = new ArrayList<>(List.of(first, second));

        candidates.sort(Priorities.comparator());

        assertThat(candidates, contains(first, second));
    }

    @Priority(10)
    private static class Early {
    }

    @Priority(1000)
    private static class Late {
    }

    @Priority(-1)
    private static class Negative {
    }

    private static class Plain {
    }
}
