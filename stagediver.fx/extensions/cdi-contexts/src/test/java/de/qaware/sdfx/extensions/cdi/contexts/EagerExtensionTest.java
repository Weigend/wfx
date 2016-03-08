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

import de.qaware.sdfx.extensions.cdi.contexts.api.Eager;
import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.lookup.cdi.CDILookupStrategy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Singleton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Unit test for the {@link EagerExtension}.
 *
 * @author christian.fritz
 */
@RunWith(Arquillian.class)
public class EagerExtensionTest {
    @Inject
    private LookupStrategy strategy;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(TestBean.class)
                .addClass(CDILookupStrategy.class)
                .addAsManifestResource("META-INF/services/javax.enterprise.inject.spi.Extension")
                .addAsManifestResource("META-INF/beans.xml");
    }

    @Before
    public void setUp() throws Exception {
        Lookup.init(strategy);
    }

    @Test
    public void testEagerInit() throws Exception {
        assertThat(TestBean.isConstructed(), is(true));
    }

    @Test
    public void testLazyInit() throws Exception {
        assertThat(TestBean1.isConstructed(), is(false));
        Lookup.lookup(TestBean1.class).toString();
        assertThat(TestBean1.isConstructed(), is(true));
    }

    @Singleton
    @Eager
    public static class TestBean {
        private static boolean constructed = false;

        @PostConstruct
        public void init() {
            constructed = true;
        }

        public static boolean isConstructed() {
            return constructed;
        }
    }

    @ApplicationScoped
    public static class TestBean1 {
        private static boolean constructed = false;

        @PostConstruct
        public void init() {
            constructed = true;
        }

        public static boolean isConstructed() {
            return constructed;
        }
    }
}