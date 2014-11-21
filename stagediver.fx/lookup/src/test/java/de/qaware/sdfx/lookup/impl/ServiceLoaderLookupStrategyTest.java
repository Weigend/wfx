package de.qaware.sdfx.lookup.impl;

import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.lookup.TestService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class ServiceLoaderLookupStrategyTest {

    private LookupStrategy lookupStrategy;

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
    public void testLookupAll() throws Exception {
        List<TestService> actual = lookupStrategy.lookupAll(TestService.class);
        assertThat(actual, hasSize(2));
        assertThat(actual.get(0), instanceOf(TestServiceImpl.class));
        assertThat(actual.get(1), instanceOf(GuiceLookupStrategyTest.TestServiceImpl.class));
    }

    public static class TestServiceImpl implements TestService {
        @Override
        public String sayHello() {
            return "World";
        }
    }
}