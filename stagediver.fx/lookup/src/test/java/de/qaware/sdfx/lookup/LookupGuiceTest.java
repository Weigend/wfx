package de.qaware.sdfx.lookup;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the main functionality of the lookup module within the non osgi mode. In this case the lookup module will
 * use google guice as backend.
 */
@RunWith(MockitoJUnitRunner.class)
public class LookupGuiceTest {

    @Mock
    private Injector injector;
    private Lookup lookup;

    @Before
    public void setUp() throws Exception {
        LookupContextHelper.setWithinOsgi(false);
        LookupContextHelper.setInjector(injector);
        lookup = new Lookup(LookupGuiceTest.class);
    }

    @After
    public void tearDown() throws Exception {
        LookupContextHelper.setInjector(null);
        lookup = null;
    }

    @Test
    public void testInit() throws Exception {
        Lookup.init(mock(AbstractModule.class));
        Injector injector = LookupContextHelper.getInjector();
        assertNotNull(injector);
    }

    @Test
    public void testLookup() throws Exception {
        TestService expected = mock(TestService.class);
        when(injector.getInstance(TestService.class)).thenReturn(expected);
        assertSame(expected, lookup.lookup(TestService.class));
    }
}
