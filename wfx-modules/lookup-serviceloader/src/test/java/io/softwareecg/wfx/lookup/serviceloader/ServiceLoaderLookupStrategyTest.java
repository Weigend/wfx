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
package io.softwareecg.wfx.lookup.serviceloader;

import io.softwareecg.wfx.lookup.TestService;
import io.softwareecg.wfx.lookup.TypedTestService;
import io.softwareecg.wfx.lookup.serviceloader.ServiceLoaderLookupStrategy.Producer;
import jakarta.annotation.Priority;
import io.softwareecg.wfx.lookup.api.TypeRef;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

/**
 * Unit test for the {@link ServiceLoaderLookupStrategy}.
 *
 */
public class ServiceLoaderLookupStrategyTest {

    private ServiceLoaderLookupStrategy lookupStrategy;

    @Before
    public void setUp() throws Exception {
        lookupStrategy = new ServiceLoaderLookupStrategy();
    }

    @Test
    public void testLookup() throws Exception {
        TestService actual = lookupStrategy.lookup(TestService.class);
        assertThat(actual, is(notNullValue()));
        assertThat(actual, instanceOf(TestServiceImpl.class));
    }

    @Test
    public void testTypedLookup() throws Exception {
        TypedTestService<String> actual = lookupStrategy.lookup(new TypeRef<TypedTestService<String>>() {});
        assertThat(actual, is(notNullValue()));
        assertThat(actual, instanceOf(TypedTestServiceImpl.class));
    }

    @Test
    public void testLookupTwice() throws Exception {
        TestService actual1 = lookupStrategy.lookup(TestService.class);
        TestService actual2 = lookupStrategy.lookup(TestService.class);
        assertThat(actual1, is(notNullValue()));
        assertThat(actual2, is(notNullValue()));
        assertThat(actual1, instanceOf(TestServiceImpl.class));
        assertThat(actual2, instanceOf(TestServiceImpl.class));
        assertThat(actual1, is(actual2));
    }

    @Test
    public void testTypedLookupTwice() throws Exception {
        TypedTestService<String> actual1 = lookupStrategy.lookup(new TypeRef<TypedTestService<String>>() {});
        TypedTestService<String> actual2 = lookupStrategy.lookup(new TypeRef<TypedTestService<String>>() {});
        assertThat(actual1, is(notNullValue()));
        assertThat(actual2, is(notNullValue()));
        assertThat(actual1, instanceOf(TypedTestServiceImpl.class));
        assertThat(actual2, instanceOf(TypedTestServiceImpl.class));
        assertThat(actual1, is(actual2));
    }

    @Test
    public void testLookupInvalidService() throws Exception {
        ServiceLoaderLookupStrategyTest actual = lookupStrategy.lookup(ServiceLoaderLookupStrategyTest.class);
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void testLookupAll() throws Exception {
        List<TestService> actual = lookupStrategy.lookupAll(TestService.class);
        assertThat(actual, hasSize(2));
        assertThat(actual.get(0), instanceOf(TestServiceImpl.class));
        assertThat(actual.get(1), instanceOf(TestServiceImpl2.class));
    }

    @Test
    public void testLookupAllTwice() throws Exception {
        lookupStrategy.lookupAll(TestService.class);
        List<TestService> actual = lookupStrategy.lookupAll(TestService.class);
        assertThat(actual, hasSize(2));
        assertThat(actual.get(0), instanceOf(TestServiceImpl.class));
        assertThat(actual.get(1), instanceOf(TestServiceImpl2.class));
    }

    @Test
    public void testLookupProducer() throws Exception {
        lookupStrategy.init(TestService.class, (Producer<TestService>) TestServiceImpl::new);
        TestService actual = lookupStrategy.lookup(TestService.class);
        assertThat(actual, is(notNullValue()));
        assertThat(actual, instanceOf(TestServiceImpl.class));
    }

    @Test
    public void testLookupProducerTwice() throws Exception {
        lookupStrategy.init(TestService.class, (Producer<TestService>) TestServiceImpl::new);

        TestService actual1 = lookupStrategy.lookup(TestService.class);
        TestService actual2 = lookupStrategy.lookup(TestService.class);
        assertThat(actual1, is(notNullValue()));
        assertThat(actual2, is(notNullValue()));
        assertThat(actual1, instanceOf(TestServiceImpl.class));
        assertThat(actual2, instanceOf(TestServiceImpl.class));
        assertThat(actual1, is(not(actual2)));
    }

    @Test
    public void testLookupAllProducer() throws Exception {
        lookupStrategy.lookup(TestService.class);
        lookupStrategy.init(TestService.class, (Producer<TestService>) TestServiceImpl::new);
        List<TestService> actual = lookupStrategy.lookupAll(TestService.class);
        assertThat(actual, hasSize(3));
        assertThat(actual.get(0), instanceOf(TestServiceImpl.class));
        assertThat(actual.get(1), instanceOf(TestServiceImpl.class));
        assertThat(actual.get(2), instanceOf(TestServiceImpl2.class));
    }

    @Test
    public void testLookupAllProducerTwice() throws Exception {
        lookupStrategy.lookup(TestService.class);
        lookupStrategy.init(TestService.class, (Producer<TestService>) TestServiceImpl::new);
        List<TestService> actual1 = lookupStrategy.lookupAll(TestService.class);
        List<TestService> actual2 = lookupStrategy.lookupAll(TestService.class);
        assertThat(actual1, hasSize(3));
        assertThat(actual2, hasSize(3));

        assertThat(actual1.get(0), instanceOf(TestServiceImpl.class));
        assertThat(actual1.get(1), instanceOf(TestServiceImpl.class));
        assertThat(actual1.get(2), instanceOf(TestServiceImpl2.class));

        assertThat(actual2.get(0), instanceOf(TestServiceImpl.class));
        assertThat(actual2.get(1), instanceOf(TestServiceImpl.class));
        assertThat(actual2.get(2), instanceOf(TestServiceImpl2.class));

        assertThat(actual1, containsInAnyOrder(is(actual2.get(0)), is(not(actual2.get(1))), is(actual2.get(2))));
    }

    @Test
    public void testInit() throws Exception {
        lookupStrategy.init(TestService.class, new TestServiceImpl());
        lookupStrategy.init(TestService.class, new TestServiceImpl());
        assertThat(lookupStrategy.lookupAll(TestService.class), hasSize(2));
        lookupStrategy.init(TestService.class, new TestServiceImpl(), true);
        assertThat(lookupStrategy.lookupAll(TestService.class), hasSize(1));
    }

    @Test
    public void testTypedInit() throws Exception {
        lookupStrategy.init(new TypeRef<TypedTestService<String>>() {}, new TypedTestServiceImpl());
        lookupStrategy.init(new TypeRef<TypedTestService<String>>() {}, new TypedTestServiceImpl());
        assertThat(lookupStrategy.lookupAll(new TypeRef<TypedTestService<String>>() {}), hasSize(2));
        lookupStrategy.init(new TypeRef<TypedTestService<String>>() {}, new TypedTestServiceImpl(), true);
        assertThat(lookupStrategy.lookupAll(new TypeRef<TypedTestService<String>>() {}), hasSize(1));
    }

    @Test
    public void testInitProducer() throws Exception {
        lookupStrategy.init(TestService.class, new TestServiceImpl());
        lookupStrategy.init(TestService.class, new TestServiceImpl());
        assertThat(lookupStrategy.lookupAll(TestService.class), hasSize(2));
        lookupStrategy.init(TestService.class, (Producer<TestService>) TestServiceImpl::new, true);
        assertThat(lookupStrategy.lookupAll(TestService.class), hasSize(1));
    }

    @Test
    public void testTypedInitProducer() throws Exception {
        lookupStrategy.init(new TypeRef<TypedTestService<String>>() {}, new TypedTestServiceImpl());
        lookupStrategy.init(new TypeRef<TypedTestService<String>>() {}, new TypedTestServiceImpl());
        assertThat(lookupStrategy.lookupAll(new TypeRef<TypedTestService<String>>() {}), hasSize(2));
        lookupStrategy.init(new TypeRef<TypedTestService<String>>() {}, (Producer<TypedTestService<String>>) TypedTestServiceImpl::new, true);
        assertThat(lookupStrategy.lookupAll(new TypeRef<TypedTestService<String>>() {}), hasSize(1));
    }

    @Test
    public void testInitArray() throws Exception {
        lookupStrategy.init(new Object[][]{
                {TestService.class, new TestServiceImpl()},
                {TestService.class, new TestServiceImpl2()},
        });
        assertThat(lookupStrategy.lookupAll(TestService.class), hasSize(2));
    }


    @Priority(1)
    public static class TestServiceImpl implements TestService {
        @Override
        public String sayHello() {
            return "World";
        }
    }

    @Priority(1000)
    public static class TestServiceImpl2 implements TestService {
        @Override
        public String sayHello() {
            return "World2";
        }
    }

    /** No annotation: comes after every annotated implementation. */
    public static class UnannotatedService implements TestService {
        @Override
        public String sayHello() {
            return "unannotated";
        }
    }

    @Priority(-5)
    public static class UrgentService implements TestService {
        @Override
        public String sayHello() {
            return "urgent";
        }
    }

    @Test
    public void testSmallerPriorityValueComesFirstAndUnannotatedLast() {
        // registered deliberately in the "wrong" order
        lookupStrategy.init(TestService.class, new UnannotatedService(), true);
        lookupStrategy.init(TestService.class, new TestServiceImpl2());
        lookupStrategy.init(TestService.class, new UrgentService());
        lookupStrategy.init(TestService.class, new TestServiceImpl());

        List<TestService> all = lookupStrategy.lookupAll(TestService.class);

        assertThat(all.get(0), instanceOf(UrgentService.class));          // -5
        assertThat(all.get(1), instanceOf(TestServiceImpl.class));        // 1
        assertThat(all.get(2), instanceOf(TestServiceImpl2.class));       // 1000
        assertThat(all.get(3), instanceOf(UnannotatedService.class));     // none
    }

    @Test
    public void testLookupReturnsTheHeadOfLookupAll() {
        // Regression: lookup() used to return the first registered instance
        // regardless of @Priority, so it could disagree with lookupAll().
        lookupStrategy.init(TestService.class, new TestServiceImpl2(), true);
        lookupStrategy.init(TestService.class, new UrgentService());

        TestService single = lookupStrategy.lookup(TestService.class);

        assertThat(single, instanceOf(UrgentService.class));
        assertThat(single, is(lookupStrategy.lookupAll(TestService.class).get(0)));
    }

    @Test
    public void testEqualPrioritiesKeepRegistrationOrder() {
        UnannotatedService first = new UnannotatedService();
        UnannotatedService second = new UnannotatedService();
        lookupStrategy.init(TestService.class, first, true);
        lookupStrategy.init(TestService.class, second);

        assertThat(lookupStrategy.lookupAll(TestService.class), contains(first, second));
        assertThat(lookupStrategy.lookup(TestService.class), is((TestService) first));
    }

    public static class TypedTestServiceImpl implements TypedTestService<String> {
        @Override
        public String sayGoodbye() {
            return "Goodbye";
        }
    }
}
