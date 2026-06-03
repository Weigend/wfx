/*
 * #%L
 * The core lookup module of the wfx platform and all applications.
 * %%
 * Copyright (C) 2013 - 2015 Weigend AM
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

import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.lookup.api.LookupStrategy;
import io.softwareecg.wfx.lookup.api.TypeRef;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit test for the {@link io.softwareecg.wfx.lookup.api.Lookup} class.
 *
 */
public class LookupTest {
    private RecordingLookupStrategy strategy;
    private TestService service;

    @Before
    public void setUp() throws Exception {
        service = new TestService() {
            @Override
            public String sayHello() {
                return "hello";
            }
        };
        strategy = new RecordingLookupStrategy(service);
        Lookup.init(strategy);
    }

    @Test
    public void testLookup() throws Exception {
        TestService actual = Lookup.lookup(TestService.class);
        assertThat(actual, is(service));
        assertThat(strategy.lookupClass, equalTo((Class<?>) TestService.class));
    }

    @Test
    public void testLookupAll() throws Exception {
        List<TestService> actual = Lookup.lookupAll(TestService.class);
        assertThat(actual, hasItem(service));
        assertThat(strategy.lookupAllClass, equalTo((Class<?>) TestService.class));
    }

    @Test
    public void testGetLookupStrategy() throws Exception {
        Lookup.init(null);
        LookupStrategy actual = Lookup.getLookupStrategy();
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void testStrategyNull() throws Exception {
        Lookup.init(null);
        assertThat(Lookup.lookup(TestService.class), is(nullValue()));
        assertThat(Lookup.lookupAll(TestService.class), is(nullValue()));
        assertThat(Lookup.lookup(new TypeRef<TestService>() {}), is(nullValue()));
        assertThat(Lookup.lookup(new TypeRef<TestService>() {}), is(nullValue()));
    }

    private static final class RecordingLookupStrategy implements LookupStrategy {
        private final TestService service;
        private Class<?> lookupClass;
        private Class<?> lookupAllClass;

        private RecordingLookupStrategy(TestService service) {
            this.service = service;
        }

        @Override
        public <T> T lookup(TypeRef<T> type) {
            return null;
        }

        @Override
        public <T> T lookup(Class<T> clazz) {
            lookupClass = clazz;
            return clazz.cast(service);
        }

        @Override
        public <T> List<T> lookupAll(Class<T> clazz) {
            lookupAllClass = clazz;
            return List.of(clazz.cast(service));
        }

        @Override
        public <T> List<T> lookupAll(TypeRef<T> type) {
            return List.of();
        }
    }
}
