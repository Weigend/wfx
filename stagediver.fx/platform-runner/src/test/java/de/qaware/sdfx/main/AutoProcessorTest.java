package de.qaware.sdfx.main;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;

import java.io.File;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AutoProcessorTest {

    private AutoProcessor processor;

    @Mock
    private FrameworkFactory factory;

    @Mock
    private Framework framework;

    private BundleContext frameworkContext;

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
        frameworkContext = initContext(framework);
        processor = new AutoProcessor(frameworkContext, configProps);
    }

    private void initConfig() {
        configProps = new HashMap<>();
        configProps.put(AutoProcessor.AUTO_DEPLOY_BUNDLE_STARTLEVEL,
                "standardBundle@1 notExistingBundle@3");
    }

    private Bundle initBundle(long id, String name, String version, boolean isFragment) throws BundleException {
        Bundle bundle = mock(Bundle.class);

        Dictionary<String, String> headers = new Hashtable<>();
        when(bundle.getBundleId()).thenReturn(id);
        when(bundle.getSymbolicName()).thenReturn(name);
        when(bundle.getVersion()).thenReturn(Version.parseVersion(version));
        when(bundle.getHeaders()).thenReturn(headers);
        if (isFragment) {
            headers.put(Constants.FRAGMENT_HOST, "de.qaware.example.fragmentHost");
            doThrow(BundleException.class).when(bundle).start();
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

    @Test
    public void testStartBundles() throws Exception {
        Bundle b1 = initBundle(2, "active", "1.0.0", false);
        when(b1.getState()).thenReturn(Bundle.ACTIVE);
        processor.getStartBundleList().add(b1);

        Bundle b2 = initBundle(3, "inactive", "1.0.0", false);
        processor.getStartBundleList().add(b2);
        Bundle b3 = initBundle(4, "fragment", "1.0.0", true);
        processor.getStartBundleList().add(b3);

        processor.startBundles();

        verify(b1, never()).start();
        verify(b2, times(1)).start();
        verify(b3, times(1)).start();
    }

    @Test
    public void testInstallUpdateBundleInstall() throws Exception {
        bundle = initBundle(4, "fragment", "1.0.0", true);
        when(frameworkContext.installBundle(anyString())).thenReturn(bundle);
        processor.installUpdateBundle(new File("t.jar"), null);
        assertThat(processor.getStartBundleList().contains(bundle), is(false));
    }

    @Test
    public void testInstallUpdateBundleUpdate() throws Exception {
        Bundle bundle = initBundle(3, "inactive", "1.0.0", false);
        BundleStartLevel bundleSl = mock(BundleStartLevel.class);
        when(bundle.adapt(BundleStartLevel.class)).thenReturn(bundleSl);
        processor.installUpdateBundle(new File("t.jar"), bundle);
        assertThat(processor.getStartBundleList().contains(bundle), is(true));
        verify(bundle, times(1)).update();
        verify(bundleSl, times(1)).setStartLevel(anyInt());
    }

    @Test
    public void testAbsoluteBundleDir() throws Exception {
        String expected = new File(getClass().getResource("/test.properties").toString()).getAbsolutePath();

        assertThat(AutoProcessor.absoluteBundleDir("target").endsWith(File.separator + "target"), is(true));
        assertThat(AutoProcessor.absoluteBundleDir(expected), equalTo(expected));
    }

    @Test
    public void testUninstallOldBundles() throws Exception {
        Map<String, Bundle> bundleMap = new HashMap<>();
        bundleMap.put("1", initBundle(2, "installed", "0.1.1", false));
        bundleMap.put("2", initBundle(0, "uninstalled", "0.1.1", false));

        processor.uninstallOldBundles(bundleMap);

        verify(bundleMap.get("1"), times(1)).uninstall();
        verify(bundleMap.get("2"), never()).uninstall();
    }
}
