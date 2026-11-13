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

import jakarta.annotation.Priority;

import java.util.Comparator;

/**
 * The one rule by which WFX reads {@link Priority @Priority}.
 * <p>
 * <b>A smaller value takes precedence.</b> Wherever several candidates are
 * ordered or one of them has to be picked, the candidate with the smaller
 * value comes first:
 * <ul>
 *     <li>{@link Lookup#lookupAll(Class)} lists it first,</li>
 *     <li>{@link Lookup#lookup(Class)} returns it when several
 *     implementations are otherwise equally eligible,</li>
 *     <li>the launcher preloads, starts and stops a module with a smaller
 *     value before the others,</li>
 *     <li>{@link Lookup#init()} prefers the {@link LookupStrategy} with the
 *     smaller value.</li>
 * </ul>
 * A class without the annotation has the priority {@link #DEFAULT} and thus
 * comes after every annotated one. Candidates with equal priority keep the
 * order in which the underlying registry supplied them.
 */
public final class Priorities {

    /**
     * The priority of a class that carries no {@link Priority @Priority}: last.
     */
    public static final int DEFAULT = Integer.MAX_VALUE;

    private static final Comparator<Object> BY_PRIORITY = Comparator.comparingInt(Priorities::of);

    private Priorities() {
    }

    /**
     * The priority declared on the given class.
     *
     * @param type the class to inspect
     * @return the value of its {@link Priority @Priority}, or {@link #DEFAULT}
     */
    public static int of(Class<?> type) {
        Priority priority = type.getAnnotation(Priority.class);
        return priority != null ? priority.value() : DEFAULT;
    }

    /**
     * The priority declared on the class of the given instance.
     *
     * @param instance the instance to inspect
     * @return the value of its class's {@link Priority @Priority}, or {@link #DEFAULT}
     */
    public static int of(Object instance) {
        return of(instance.getClass());
    }

    /**
     * Orders instances by precedence: smaller {@link Priority @Priority}
     * value first, unannotated classes last. Use it with a stable sort so that
     * equal priorities keep their original order.
     *
     * @param <T> the type of the instances to order
     * @return the comparator
     */
    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> comparator() {
        return (Comparator<T>) BY_PRIORITY;
    }
}
