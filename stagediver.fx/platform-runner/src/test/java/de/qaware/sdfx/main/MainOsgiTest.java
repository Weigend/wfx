package de.qaware.sdfx.main;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.FrameworkFactory;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class MainOsgiTest {

    private static final String PROPERTY_THAT_NOT_EXISTS = "sys.prop.not.exists";
    private MainOsgi runner;

    @Before
    public void setUp() throws Exception {
        runner = new MainOsgi();
        runner.initFramework();
    }

    @After
    public void tearDown() throws Exception {
        runner = null;
    }

    @Test
    public void testRunFramework() throws Exception {
        when(runner.getFramework().waitForStop(0)).thenReturn(
                new FrameworkEvent(FrameworkEvent.STOPPED_UPDATE, mock(Bundle.class), null),
                new FrameworkEvent(FrameworkEvent.STOPPED, mock(Bundle.class), null));

        runner.runFramework();
        verify(runner.getFramework(), times(2)).start();
        verify(runner.getFramework(), times(2)).waitForStop(0);
    }

    @Test
    public void testInitFramework() throws Exception {
        runner = new MainOsgi();
        runner.initFramework();
        verify(runner.getFramework(), times(1)).init();
    }

    @Test
    public void testGetFrameworkFactory() throws Exception {
        FrameworkFactory factory = runner.getFrameworkFactory();
        assertTrue(factory instanceof MockFrameworkFactory);
    }

    @Test
    public void testLoadProperties() throws Exception {

        Map<String, String> props = MainOsgi.loadProperties(getClass().getResource("/test.properties"));
        assertEquals(2, props.size());
        assertEquals("asdf", props.get("prop.1"));
        assertEquals("test-asdf", props.get("prop.replaced"));
    }

    @Test
    public void testLoadPropertiesInvalidUrl() throws Exception {
        Map<String, String> props = MainOsgi.loadProperties(new URL("file:///test.properties"));
        assertNull(props);
    }

    @Test
    public void testGetPropertyFileUrl() throws Exception {
        assertThat(System.getProperty(PROPERTY_THAT_NOT_EXISTS), is(nullValue()));

        URL expected = getClass().getResource("/test.properties");
        URL actual = MainOsgi.getPropertyFileUrl(PROPERTY_THAT_NOT_EXISTS, "test.properties");
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testGetPropertyFileUrl1() throws Exception {
        assertThat(System.getProperty(PROPERTY_THAT_NOT_EXISTS), is(nullValue()));
        URL expected = getClass().getResource("/test.properties");
        URL actual = MainOsgi.getPropertyFileUrl(PROPERTY_THAT_NOT_EXISTS, "/test.properties");
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testGetPropertyFileUrlCustomProperty() throws Exception {
        URL expected = getClass().getResource("/test.properties");
        System.setProperty("sdfx.test.platform.runner.prop", expected.toString());
        URL actual = MainOsgi.getPropertyFileUrl("sdfx.test.platform.runner.prop", "didnotexist.properties");
        assertThat(actual, equalTo(expected));
    }

    @Test
    public void testGetPropertyFileUrlCustomPropertyError() throws Exception {
        System.setProperty("sdfx.test.platform.runner.prop", "undefinedProtocol:///didnotExist.properties");
        URL actual = MainOsgi.getPropertyFileUrl("sdfx.test.platform.runner.prop", "didnotexist.properties");
        assertThat(actual, is(nullValue()));
    }

    @Test
    public void testGetPropertyFileUrlConfigDir() throws Exception {
        assertThat(System.getProperty(PROPERTY_THAT_NOT_EXISTS), is(nullValue()));
        assertThat(System.getProperty("user.dir"), is(notNullValue()));

        File expected = new File(System.getProperty("user.dir"), "config/withinConfig.properties");
        URL actual = MainOsgi.getPropertyFileUrl(PROPERTY_THAT_NOT_EXISTS, "withinConfig.properties");
        assertThat(actual, equalTo(expected.toURI().toURL()));
    }

    @Test
    public void testLoadPropertiesExists() throws Exception {
        assertThat(System.getProperty(PROPERTY_THAT_NOT_EXISTS), is(nullValue()));
        Map p = MainOsgi.loadProperties(PROPERTY_THAT_NOT_EXISTS, "/test.properties");
        assertThat(p.size(), is(2));
    }

    @Test
    public void testLoadPropertiesNotExists() throws Exception {
        System.setProperty("sdfx.test.platform.runner.prop", "undefinedProtocol:///didnotExist.properties");
        Map p = MainOsgi.loadProperties("sdfx.test.platform.runner.prop", "didnotexist.properties");
        assertThat(p, is(nullValue()));
    }

    @Test
    public void testCopyProperties() throws Exception {
        runner.configProps = new HashMap<>();
        System.setProperty("stagediver.test.property", "test");
        System.setProperty("felix.test.property", "test");
        System.setProperty("org.osgi.framework.test.property", "test");
        runner.copySystemProperties();
        assertThat(runner.configProps.size(), greaterThanOrEqualTo(3));
    }

    @Test
    public void testAddShutdownHook() throws Exception {
        runner.configProps = new HashMap<>();
        runner.configProps.put(MainOsgi.SHUTDOWN_HOOK_PROP, "true");
        runner.addShutdownHook();
        assertThat(runner.shutdownThread, is(notNullValue()));
        assertThat(Runtime.getRuntime().removeShutdownHook(runner.shutdownThread), is(true));
        runner.shutdownThread.start();
        runner.shutdownThread.join();
        verify(runner.getFramework()).stop();
    }

    @Test
    public void testAddShutdownHookDisabled() throws Exception {
        runner.configProps = new HashMap<>();
        runner.configProps.put(MainOsgi.SHUTDOWN_HOOK_PROP, "false");
        runner.addShutdownHook();
        assertThat(runner.shutdownThread, is(nullValue()));
    }
}
