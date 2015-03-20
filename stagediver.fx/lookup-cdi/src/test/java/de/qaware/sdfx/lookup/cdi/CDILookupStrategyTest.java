package de.qaware.sdfx.lookup.cdi;

import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.lookup.TestService;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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
    private WeldContainer container;

    @Before
    public void setUp() throws Exception {
        container = new Weld().initialize();
        strategy = container.instance().select(CDILookupStrategy.class).get();
    }

    @Test
    public void testLookup() throws Exception {
        TestService service = strategy.lookup(TestService.class);
        assertThat(service.sayHello(), is(equalTo("Hello CDI")));
        assertThat(service, instanceOf(Service1.class));
    }

    @Test
    @Ignore("Until the strategy did not return 2 serices")
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

    public static class Service1 implements TestService {
        @Override
        public String sayHello() {
            return "Hello CDI";
        }
    }

    @TestQualifier
    public static class Service2 implements TestService {
        @Override
        public String sayHello() {
            return "Hello Alternative";
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Qualifier
    public static @interface TestQualifier {
    }
}