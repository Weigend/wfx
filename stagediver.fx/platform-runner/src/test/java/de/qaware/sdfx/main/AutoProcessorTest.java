package de.qaware.sdfx.main;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AutoProcessorTest {

    private AutoProcessor processor;

    @Mock
    private FrameworkFactory factory;

    @Mock
    private Framework framework;

    private Bundle bundle;

    private Bundle fragmentBundle;

    private Map<Long, Bundle> bundles = new HashMap<>();

    private Map<String, String> configProps;


    @Before
    public void setUp() throws Exception {
        bundles.put(0L, framework);
        when(factory.newFramework(anyMap())).thenReturn(framework);
        bundle = initBundle(1, "standardBundle", "0.1.2.SNAPSHOT", false);
        fragmentBundle = initBundle(2, "fragmentBundle", "0.1.1.SNAPSHOT", true);
        initConfig();
        processor = new AutoProcessor(initContext(framework), configProps);
    }

    private void initConfig() {
        configProps = new HashMap<>();
        configProps.put(AutoProcessor.AUTO_DEPLOY_BUNDLE_STARTLEVEL,
                "standardBundle@1 notExistingBundle@3");
    }

    private Bundle initBundle(long id, String name, String version, boolean isFragment) {
        Bundle bundle = mock(Bundle.class);

        Dictionary<String, String> headers = new Hashtable<>();
        when(bundle.getBundleId()).thenReturn(id);
        when(bundle.getSymbolicName()).thenReturn(name);
        when(bundle.getVersion()).thenReturn(Version.parseVersion(version));
        when(bundle.getHeaders()).thenReturn(headers);
        if (isFragment) {
            headers.put(Constants.FRAGMENT_HOST, "de.qaware.example.fragmentHost");
        }
        bundles.put(id, bundle);
        return bundle;
    }

    private BundleContext initContext(Bundle bundle) {
        BundleContext context = mock(BundleContext.class);
        when(context.getBundle()).thenReturn(bundle);
        Collection<Bundle> values = bundles.values();
        when(context.getBundles()).thenReturn(values.toArray(new Bundle[values.size()]));
        for (Bundle b : bundles.values()) {
            when(context.getBundle(b.getBundleId())).thenReturn(b);
            when(context.getBundle(b.getSymbolicName())).thenReturn(b);
        }
        when(bundle.getBundleContext()).thenReturn(context);
        return context;
    }

    @After
    public void tearDown() throws Exception {
        bundle = null;
        fragmentBundle = null;
        bundles = new HashMap<>();
    }

    @Test
    public void testIsFragment() throws Exception {
        assertFalse(AutoProcessor.isFragment(bundle));
        assertTrue(AutoProcessor.isFragment(fragmentBundle));
    }

    @Test
    public void testGetStartLevel() throws Exception {
        FrameworkStartLevel fwsl = mock(FrameworkStartLevel.class);
        when(framework.adapt(FrameworkStartLevel.class)).thenReturn(fwsl);
        when(fwsl.getInitialBundleStartLevel()).thenReturn(5);
        when(fwsl.getStartLevel()).thenReturn(1);

        processor.initStartLevels();
        assertEquals(1, processor.getStartLevel(bundle));
        assertEquals(5, processor.getStartLevel(fragmentBundle));
    }

    @Test
    public void testGetStartLevelExternalConfig() throws Exception {
        configProps.put(AutoProcessor.AUTO_DEPLOY_STARTLEVEL_PROPERY, "3");
        processor = new AutoProcessor(initContext(framework), configProps);

        FrameworkStartLevel fwsl = mock(FrameworkStartLevel.class);
        when(framework.adapt(FrameworkStartLevel.class)).thenReturn(fwsl);
        when(fwsl.getInitialBundleStartLevel()).thenReturn(5);
        when(fwsl.getStartLevel()).thenReturn(1);

        processor.initStartLevels();
        assertEquals(3, processor.getStartLevel(fragmentBundle));
    }

}
