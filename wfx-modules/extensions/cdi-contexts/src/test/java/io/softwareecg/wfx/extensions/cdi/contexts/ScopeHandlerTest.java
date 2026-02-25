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
package io.softwareecg.wfx.extensions.cdi.contexts;

import io.softwareecg.wfx.extensions.cdi.contexts.api.ViewContext;
import io.softwareecg.wfx.extensions.cdi.contexts.testviews.AppScopedView;
import io.softwareecg.wfx.extensions.cdi.contexts.testviews.ViewScopedView;
import io.softwareecg.wfx.extensions.cdi.contexts.view.ViewContextExtension;
import io.softwareecg.wfx.extensions.cdi.contexts.view.ViewContextImpl;
import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.lookup.LookupStrategy;
import io.softwareecg.wfx.lookup.cdi.CDILookupStrategy;
import io.softwareecg.wfx.windowmtg.api.View;
import io.softwareecg.wfx.windowmtg.api.WindowManager;
import io.softwareecg.wfx.windowmtg.impl.WindowManagerImpl;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.softwareecg.wfx.extensions.cdi.contexts.BeanUtils.getUnwrappedInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

/**
 * Unit test for the {@link ScopeHandler}.
 *
 */
@RunWith(Arquillian.class)
public class ScopeHandlerTest {
    @Inject
    private LookupStrategy strategy;
    @Inject
    private ViewContext context;
    @Inject
    private ScopeHandler handler;
    @Inject
    private WindowManager windowManager;
    @Inject
    private AppScopedView appView;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(ScopeHandler.class)
                .addClass(AppScopedView.class)
                .addClass(ViewScopedView.class)
                .addClass(ViewContextImpl.class)
                .addClass(WindowManagerImpl.class)
                .addClass(ViewContextExtension.Producer.class)
                .addClass(CDILookupStrategy.class)
                .addAsManifestResource("META-INF/services/jakarta.enterprise.inject.spi.Extension")
                .addAsManifestResource("META-INF/beans.xml");
    }

    @Before
    public void setUp() throws Exception {
        Lookup.init(strategy);
        context.associate("1");
        context.activate();
    }

    @Test
    public void testInitScopeHandler() throws Exception {
        windowManager.setFocusedView(appView);
        ViewScopedView view1 = getUnwrappedInstance(Lookup.lookup(ViewScopedView.class));
        windowManager.setFocusedView(view1);
        context.associate("2", true);
        ViewScopedView view2 = getUnwrappedInstance(Lookup.lookup(ViewScopedView.class));
        windowManager.setFocusedView(view2);
        assertThat(context.getAssociatedStorage(), is(equalTo("2")));

        windowManager.setFocusedView(view1);
        assertThat(context.getAssociatedStorage(), is(equalTo("1")));

        windowManager.setFocusedView(appView);
        windowManager.setFocusedView(mock(View.class));
        assertThat(context.getAssociatedStorage(), is(equalTo("1")));
    }
}