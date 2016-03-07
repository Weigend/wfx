package de.qaware.sdfx.extensions.cdi.contexts;

import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.junit.Test;

import static de.qaware.sdfx.extensions.cdi.contexts.BeanUtils.getUnwrappedInstance;
import static de.qaware.sdfx.extensions.cdi.contexts.BeanUtils.isProxied;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for the {@link BeanUtils}
 *
 * @author christian.fritz
 */
public class BeanUtilsTest {

    @Test
    public void testIsProxied() throws Exception {
        assertThat(isProxied(""), is(false));
        assertThat(isProxied(mock(TargetInstanceProxy.class)), is(true));
    }

    @Test
    public void testGetUnwrappedInstance() throws Exception {
        TargetInstanceProxy<String> proxy = mock(TargetInstanceProxy.class);
        String test = "test";
        when(proxy.getTargetInstance()).thenReturn(test);

        assertThat(getUnwrappedInstance(test), sameInstance(test));
        assertThat(getUnwrappedInstance(proxy), sameInstance(test));
    }
}