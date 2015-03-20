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