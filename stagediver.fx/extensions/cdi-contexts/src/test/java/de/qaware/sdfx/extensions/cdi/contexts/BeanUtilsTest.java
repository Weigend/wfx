/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 QAware GmbH
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