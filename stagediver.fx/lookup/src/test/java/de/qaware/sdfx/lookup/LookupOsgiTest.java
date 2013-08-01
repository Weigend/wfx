package de.qaware.sdfx.lookup;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.sun.istack.internal.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LookupOsgiTest {


    private Multimap<Class, ServiceReference<? extends Object>> services = ArrayListMultimap.create();
    private Lookup lookup = new Lookup(LookupOsgiTest.class);
    @Mock
    private BundleContext context;

    @Before
    public void setUp() throws Exception {
        Lookup.setWithinOsgi(true);
        Lookup.setInjector(null);
        lookup.setContext(context);
    }

    @After
    public void tearDown() throws Exception {
        Lookup.setWithinOsgi(false);
        Lookup.setInjector(null);
    }

    @Test
    public void testLookupNoRegistration() throws Exception {
        TestService obj = lookup.lookup(TestService.class);
        assertNull(obj);
    }

    @Test
    public void testLookup() throws Exception {
        TestService service = injectService(TestService.class);
        assertSame(service, lookup.lookup(TestService.class));
    }

    @Test
    public void testLookupAllNoRegistration() throws Exception {
        List<TestService> obj = lookup.lookupAll(TestService.class);
        assertEquals(0, obj.size());
    }

    @Test
    public void testLookupAllRanking() throws Exception {
        List<TestService> expected = new ArrayList<>();
        expected.add(injectService(TestService.class));
        expected.add(injectService(TestService.class, -5));
        expected.add(injectService(TestService.class, 3));
        expected.add(injectService(TestService.class, 10));

        List<TestService> actual = lookup.lookupAll(TestService.class);

        assertEquals(4, actual.size());
        assertSame(expected.get(3), actual.get(0));
        assertSame(expected.get(2), actual.get(1));
        assertSame(expected.get(0), actual.get(2));
        assertSame(expected.get(1), actual.get(3));
    }

    private <T> T injectService(Class<T> clazz) throws InvalidSyntaxException {
        return injectService(clazz, null);
    }

    private <T> T injectService(Class<T> clazz, Integer ranking) throws InvalidSyntaxException {
        ServiceReference<T> reference = mock(ServiceReference.class);
        T service = mock(clazz);
        when(reference.getProperty("service.ranking")).thenReturn(ranking);
        services.put(clazz, reference);
        when(context.getServiceReference(clazz)).thenReturn(reference);

        // Transform ServiceReference to ServiceReference<T>
        Collection<ServiceReference<T>> serviceReferences = Collections2.transform(services.get(clazz),
                new Function<ServiceReference<? extends Object>, ServiceReference<T>>() {
                    @Override
                    public ServiceReference<T> apply(@Nullable org.osgi.framework.ServiceReference<? extends Object> serviceReference) {
                        return (ServiceReference<T>) serviceReference;
                    }
                });

        when(context.getServiceReferences(clazz, null)).thenReturn(serviceReferences);
        when(context.getService(reference)).thenReturn(service);
        return service;
    }
}
