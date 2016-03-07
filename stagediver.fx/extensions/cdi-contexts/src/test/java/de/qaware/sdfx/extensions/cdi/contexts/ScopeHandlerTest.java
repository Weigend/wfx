package de.qaware.sdfx.extensions.cdi.contexts;

import de.qaware.sdfx.extensions.cdi.contexts.api.ViewContext;
import de.qaware.sdfx.extensions.cdi.contexts.testviews.AppScopedView;
import de.qaware.sdfx.extensions.cdi.contexts.testviews.ViewScopedView;
import de.qaware.sdfx.extensions.cdi.contexts.view.ViewContextExtension;
import de.qaware.sdfx.extensions.cdi.contexts.view.ViewContextImpl;
import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.lookup.cdi.CDILookupStrategy;
import de.qaware.sdfx.windowmtg.api.View;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import de.qaware.sdfx.windowmtg.impl.WindowManagerImpl;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static de.qaware.sdfx.extensions.cdi.contexts.BeanUtils.getUnwrappedInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

/**
 * Unit test for the {@link ScopeHandler}.
 *
 * @author christian.fritz
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
                .addAsManifestResource("META-INF/services/javax.enterprise.inject.spi.Extension")
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