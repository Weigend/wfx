/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 Weigend AM
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
package de.weigend.wfx.extensions.cdi.contexts;

import de.weigend.wfx.extensions.cdi.contexts.api.ViewContext;
import de.weigend.wfx.extensions.cdi.contexts.api.ViewScoped;
import de.weigend.wfx.extensions.cdi.contexts.view.ViewContextExtension;
import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.lookup.LookupStrategy;
import de.weigend.wfx.lookup.cdi.CDILookupStrategy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link ViewContext} which implements {@link ViewScoped}.
 * <p>
 * It also tests the {@link ViewContextExtension}.
 *
 * @author Software-EKG Team
 */
@RunWith(Arquillian.class)
public class AbstractJfxContextTest {

    @Inject
    private LookupStrategy strategy;
    @Inject
    private ViewContext context;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(AppScope.class)
                .addClass(ViewScoped.class)
                .addClass(CDILookupStrategy.class)
                .addClass(ViewContextExtension.Producer.class)
                .addAsManifestResource("META-INF/services/jakarta.enterprise.inject.spi.Extension")
                .addAsManifestResource("META-INF/beans.xml");
    }

    @Before
    public void setUp() throws Exception {
        Lookup.init(strategy);
        context.associate("1");
        context.activate();
    }

    @After
    public void tearDown() throws Exception {
        context.dissociate("-1");
    }

    @Test
    public void testAfterBeanDiscovery() throws Exception {
        assertThat(context, is(notNullValue()));
        assertThat(context.getScope(), is(equalTo(ViewScoped.class)));
        assertThat(context.isActive(), is(true));
    }

    @Test
    public void testContext() throws Exception {
        AppScope app = Lookup.lookup(AppScope.class);
        app.getAnalyzer().setTest(1);
        assertThat(context.getAssociatedStorage(), is(equalTo("1")));
        context.associate("2", true);

        assertThat(app.getAnalyzer().getTest(), is(equalTo(0)));
        app.getAnalyzer().setTest(2);

        context.associate("1", true);
        assertThat(app.getAnalyzer().getTest(), is(equalTo(1)));

        context.associate("2", true);
        assertThat(app.getAnalyzer().getTest(), is(equalTo(2)));
        assertThat(context.getAssociatedStorage(), is(equalTo("2")));
    }

    @Test
    public void testDestroyContext() throws Exception {
        AppScope app = Lookup.lookup(AppScope.class);
        context.associate("1");
        app.getAnalyzer().setTest(0);
        context.associate("2", true);
        app.getAnalyzer().setTest(1);
        assertThat(context.getStorageIdentifier(), containsInAnyOrder("1", "2"));
        context.invalidate();
        context.deactivate();
        assertThat(context.getStorageIdentifier(), containsInAnyOrder("1"));
    }

    @Test
    public void testGetStorageIdentifierForInvalid() throws Exception {
        assertThat(context.getStorageIdentifierFor(""), nullValue());
        assertThat(context.getStorageIdentifierFor(Lookup.lookup(AppScope.class)), nullValue());
    }

    @Test
    public void testGetStorageIdentifierFor() throws Exception {
        TestScope test1 = getRealInstanceForStorage("1");
        TestScope test2 = getRealInstanceForStorage("2");
        TestScope test3 = getRealInstanceForStorage("3");

        assertThat(context.getStorageIdentifierFor(test1), is(equalTo("1")));
        assertThat(context.getStorageIdentifierFor(test3), is(equalTo("3")));
        assertThat(context.getStorageIdentifierFor(test2), is(equalTo("2")));
        assertThat(context.getStorageIdentifierFor(test1), is(equalTo("1")));
    }

    private TestScope getRealInstanceForStorage(String storage) {
        context.associate(storage, true);
        return BeanUtils.getUnwrappedInstance(Lookup.lookup(TestScope.class));
    }

    /**
     * Test bean that get a proxy of {@link ViewScoped} injected.
     */
    @Singleton
    public static class AppScope {
        @Inject
        private TestScope analyzer;

        public TestScope getAnalyzer() {
            return analyzer;
        }
    }

    /**
     * The {@link ViewScoped} bean for testing.
     */
    @ViewScoped
    public static class TestScope {
        private int test = 0;

        public int getTest() {
            return test;
        }

        public void setTest(int test) {
            this.test = test;
        }
    }
}