/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
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
package io.softwareecg.wfx.lookup.avaje;

import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.lookup.TestService;
import io.softwareecg.wfx.lookup.TypedTestService;
import io.softwareecg.wfx.lookup.avaje.testbeans.Service1;
import io.softwareecg.wfx.lookup.avaje.testbeans.Service2;
import io.softwareecg.wfx.lookup.avaje.testbeans.Service3;
import io.softwareecg.wfx.lookup.avaje.testbeans.Service4;
import jakarta.enterprise.util.TypeLiteral;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Drop-in equivalent of {@code CDILookupStrategyTest} against the Avaje-backed strategy.
 * Verifies that all four LookupStrategy methods resolve correctly under priority sorting.
 */
public class AvajeLookupStrategyTest {

    @BeforeClass
    public static void setUp() {
        AvajeLookupStrategy.initLookup();
    }

    @AfterClass
    public static void tearDown() {
        AvajeLookupStrategy.shutdownLookup();
    }

    @Test
    public void testLookup() {
        TestService1 service = Lookup.lookup(TestService1.class);
        assertThat(service.sayGoodbye(), is(equalTo("Goodbye Avaje")));
        assertThat(service, instanceOf(Service3.class));
    }

    @Test
    public void testLookupAll() {
        List<TestService> services = Lookup.lookupAll(TestService.class);
        assertThat(services, hasSize(2));
        // sorted by priority descending: Service1(@Priority(1)) before Service2(@Priority(-1000))
        assertThat(services.get(0), instanceOf(Service1.class));
        assertThat(services.get(0).sayHello(), is(equalTo("Hello Avaje")));
        assertThat(services.get(1), instanceOf(Service2.class));
        assertThat(services.get(1).sayHello(), is(equalTo("Hello Alternative")));
    }

    @Test
    public void testInit() {
        AvajeLookupStrategy.shutdownLookup();
        AvajeLookupStrategy.initLookup();
        assertThat(Lookup.getLookupStrategy(), is(instanceOf(AvajeLookupStrategy.class)));
    }

    @Test
    public void testTypedLookup() {
        TypedTestService<String> service = Lookup.lookup(new TypeLiteral<TypedTestService<String>>() {
        });
        assertThat(service, instanceOf(Service4.class));
        assertThat(service.sayGoodbye(), is(equalTo("Goodbye typed Avaje")));
    }

    @Test
    public void testTypedLookupAll() {
        List<TypedTestService<String>> services = Lookup.lookupAll(new TypeLiteral<TypedTestService<String>>() {
        });
        assertThat(services, hasSize(1));
        assertThat(services.get(0), instanceOf(Service4.class));
    }

    public interface TestService1 {
        String sayGoodbye();
    }
}
