/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
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
package de.weigend.wfx.lookup.cdi;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.lookup.TestService;
import de.weigend.wfx.lookup.TypedTestService;
import jakarta.annotation.Priority;
import jakarta.enterprise.util.TypeLiteral;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link de.weigend.wfx.lookup.cdi.CDILookupStrategy}.
 *
 * @author Software-EKG Team
 */
public class CDILookupStrategyTest {

    @BeforeClass
    public static void setUp() throws Exception {
        CDILookupStrategy.initLookup();
    }

    @Test
    public void testLookup() throws Exception {
        TestService1 service = Lookup.lookup(TestService1.class);
        assertThat(service.sayGoodbye(), is(equalTo("Goodbye CDI")));
        assertThat(service, instanceOf(Service3.class));
    }

    @Test
    public void testLookupAll() throws Exception {
        List<TestService> testServices = Lookup.lookupAll(TestService.class);
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
        CdiLookupTestHelper.shutDownCdiLookup();
        CDILookupStrategy.initLookup();
        assertThat(Lookup.getLookupStrategy(), is(instanceOf(CDILookupStrategy.class)));
    }

    @Test
    public void testTypedLookup() throws Exception {
        TypedTestService<String> service = Lookup.lookup(new TypeLiteral<TypedTestService<String>>() {
        });
        assertThat(service, instanceOf(Service4.class));
        assertThat(service.sayGoodbye(), instanceOf(String.class));
        assertThat(service.sayGoodbye(), is(equalTo("Goodbye typed CDI")));
    }

    @Test
    public void testTypedLookupAll() throws Exception {
        List<TypedTestService<String>> services = Lookup.lookupAll(new TypeLiteral<TypedTestService<String>>() {
        });
        assertThat(services, hasSize(1));
        TypedTestService<String> service = services.get(0);
        assertThat(service, instanceOf(Service4.class));
        assertThat(service.sayGoodbye(), instanceOf(String.class));
        assertThat(service.sayGoodbye(), is(equalTo("Goodbye typed CDI")));
    }

    public interface TestService1 {
        String sayGoodbye();
    }

    @Priority(1)
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

    public static class Service4 implements TypedTestService<String> {
        @Override
        public String sayGoodbye() {
            return "Goodbye typed CDI";
        }
    }
}
