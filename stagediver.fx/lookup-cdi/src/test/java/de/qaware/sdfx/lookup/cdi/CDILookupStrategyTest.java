package de.qaware.sdfx.lookup.cdi;

import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.lookup.TestService;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
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

    @Before
    public void setUp() throws Exception {
        WeldContainer container = new Weld().initialize();
        strategy = container.instance().select(CDILookupStrategy.class).get();
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
        assertThat(service.sayHello(), is(equalTo("Hello Alternative")));
        assertThat(service, instanceOf(Service2.class));

        service = testServices.get(1);
        assertThat(service.sayHello(), is(equalTo("Hello CDI")));
        assertThat(service, instanceOf(Service1.class));
    }

    public static interface TestService1 {
        String sayGoodbye();
    }

    public static class Service1 implements TestService {
        @Override
        public String sayHello() {
            return "Hello CDI";
        }
    }

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
}