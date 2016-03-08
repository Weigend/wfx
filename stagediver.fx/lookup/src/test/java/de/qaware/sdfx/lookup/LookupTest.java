/*
 * #%L
 * The core lookup module of the stagediver.fx platform and all applications.
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
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
package de.qaware.sdfx.lookup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.enterprise.util.TypeLiteral;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void testStrategyNull() throws Exception {
        Lookup.init((LookupStrategy) null);
        assertThat(Lookup.lookup(TestService.class), is(nullValue()));
        assertThat(Lookup.lookupAll(TestService.class), is(nullValue()));
        assertThat(Lookup.lookup(new TypeLiteral<TestService>() {}), is(nullValue()));
        assertThat(Lookup.lookup(new TypeLiteral<TestService>() {}), is(nullValue()));
    }
}