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
package de.qaware.sdfx.lookup.guice;

import com.google.inject.AbstractModule;
import de.qaware.sdfx.lookup.TestService;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;

/**
 * Unit test for the {@link GuiceLookupStrategy}.
 *
 * @author christian.fritz
 */
public class GuiceLookupStrategyTest {

    private GuiceLookupStrategy lookupStrategy;

    @Before
    public void setUp() throws Exception {
        lookupStrategy = new GuiceLookupStrategy(new TestModule());
    }

    @Test
    public void testLookup() throws Exception {
        TestService actual = lookupStrategy.lookup(TestService.class);
        MatcherAssert.assertThat(actual, is(notNullValue()));
        MatcherAssert.assertThat(actual, instanceOf(TestServiceImpl.class));
    }

    @Test
    public void testLookupAll() throws Exception {
        List<TestService> actual = lookupStrategy.lookupAll(TestService.class);
        MatcherAssert.assertThat(actual, hasSize(1));
        MatcherAssert.assertThat(actual.get(0), is(notNullValue()));
        MatcherAssert.assertThat(actual.get(0), instanceOf(TestServiceImpl.class));
    }

    @Test
    public void testListConstructor() throws Exception {
        lookupStrategy = new GuiceLookupStrategy(Arrays.asList(new TestModule()));
        TestService actual = lookupStrategy.lookup(TestService.class);
        MatcherAssert.assertThat(actual, is(notNullValue()));
        MatcherAssert.assertThat(actual, instanceOf(TestServiceImpl.class));
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(TestService.class).to(TestServiceImpl.class);
        }
    }

    public static class TestServiceImpl implements TestService {
        @Override
        public String sayHello() {
            return "World";
        }
    }
}
