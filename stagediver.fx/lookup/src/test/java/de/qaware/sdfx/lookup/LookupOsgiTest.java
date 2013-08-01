package de.qaware.sdfx.lookup;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LookupOsgiTest {


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
        ServiceReference<TestService> reference = mock(ServiceReference.class);
        TestService service = mock(TestService.class);
        when(context.getServiceReference(TestService.class)).thenReturn(reference);
        when(context.getService(reference)).thenReturn(service);

        assertSame(service, lookup.lookup(TestService.class));
    }

    @Test
    public void testLookupAllNoRegistration() throws Exception {
        List<TestService> obj = lookup.lookupAll(TestService.class);
        assertEquals(0, obj.size());
    }
}
