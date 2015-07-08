/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
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
package de.qaware.sdfx.lookup.cdi;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.lookup.Priority;
import de.qaware.sdfx.lookup.TestService;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link de.qaware.sdfx.lookup.cdi.CDILookupStrategy}.
 *
 * @author christian.fritz
 */
public class CDILookupStrategyTest {

    private LookupStrategy strategy;
    private Weld weld;

    @Before
    public void setUp() throws Exception {
        weld = new Weld();
        WeldContainer container = weld.initialize();
        strategy = container.instance().select(CDILookupStrategy.class).get();
    }

    @After
    public void tearDown() throws Exception {
        weld.shutdown();
    }

    @Test
    public void testLookup() throws Exception {
        TestService1 service = strategy.lookup(TestService1.class);
        assertThat(service.sayGoodbye(), is(equalTo("Goodbye CDI")));
        assertThat(service, instanceOf(Service3.class));
    }

    @Test
    public void testLookupAll() throws Exception {
        List<TestService> testServices = strategy.lookupAll(TestService.class);
        assertThat(testServices, hasSize(2));
        TestService service = testServices.get(0);
        assertThat(service.sayHello(), is(equalTo("Hello CDI")));
        assertThat(service, instanceOf(Service1.class));

        service = testServices.get(1);
        assertThat(service.sayHello(), is(equalTo("Hello Alternative")));
        assertThat(service, instanceOf(Service2.class));
    }

    @Test
    public void testInit() throws Exception {
        Lookup.init(null);
        CDILookupStrategy.initLookup();
        assertThat(Lookup.getLookupStrategy(), is(instanceOf(CDILookupStrategy.class)));
    }

    @Test
    public void testTypedLookup() throws Exception {
        TestService2<String> service = strategy.lookup(new TypeLiteral<TestService2<String>>() {
        });
        assertThat(service.sayGoodbye(), instanceOf(String.class));
        assertThat(service.sayGoodbye(), is(equalTo("Goodbye typed CDI")));
    }

    public interface TestService1 {
        String sayGoodbye();
    }

    public interface TestService2<T> {
        T sayGoodbye();
    }

    public static class Service1 implements TestService {
        @Override
        public String sayHello() {
            return "Hello CDI";
        }
    }

    @Priority(-1000)
    public static class Service2 implements TestService {
        @Override
        public String sayHello() {
            return "Hello Alternative";
        }
    }

    public static class Service3 implements TestService1 {
        @Override
        public String sayGoodbye() {
            return "Goodbye CDI";
        }
    }

    public static class Service4 implements TestService2<String> {
        @Override
        public String sayGoodbye() {
            return "Goodbye typed CDI";
        }
    }
}
