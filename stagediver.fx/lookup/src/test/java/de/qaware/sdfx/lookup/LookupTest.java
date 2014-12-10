package de.qaware.sdfx.lookup;

import com.google.inject.Module;
import de.qaware.sdfx.lookup.impl.GuiceLookupStrategy;
import de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit test for the {@link de.qaware.sdfx.lookup.Lookup} class.
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class LookupTest {
    @Mock
    private LookupStrategy strategy;
    @Mock
    private TestService service;

    @Before
    public void setUp() throws Exception {
        Lookup.init(strategy);
        when(strategy.lookup(TestService.class)).thenReturn(service);
        when(strategy.lookupAll(TestService.class)).thenReturn(Arrays.asList(service));
    }

    @Test
    public void testInit() throws Exception {
        Lookup.init(mock(Module.class));
        LookupStrategy actual = Lookup.getLookupStrategy();
        assertThat(actual, instanceOf(GuiceLookupStrategy.class));
    }

    @Test
    public void testInit1() throws Exception {
        Lookup.init(Arrays.asList(mock(Module.class), mock(Module.class)));
        LookupStrategy actual = Lookup.getLookupStrategy();
        assertThat(actual, instanceOf(GuiceLookupStrategy.class));
    }

    @Test
    public void testLookup() throws Exception {
        TestService actual = Lookup.lookup(TestService.class);
        assertThat(actual, is(service));
        verify(strategy).lookup(TestService.class);
    }

    @Test
    public void testLookupAll() throws Exception {
        List<TestService> actual = Lookup.lookupAll(TestService.class);
        assertThat(actual, hasItem(service));
        verify(strategy).lookupAll(TestService.class);
    }

    @Test
    public void testGetLookupStrategy() throws Exception {
        Lookup.init((LookupStrategy) null);
        LookupStrategy actual = Lookup.getLookupStrategy();
        assertThat(actual, instanceOf(ServiceLoaderLookupStrategy.class));
    }
}