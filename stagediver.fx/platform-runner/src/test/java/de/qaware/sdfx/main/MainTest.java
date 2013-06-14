package de.qaware.sdfx.main;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.launch.FrameworkFactory;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class MainTest {

    private Main runner;

    @Before
    public void setUp() throws Exception {
        runner = new Main();
    }

    @After
    public void tearDown() throws Exception {
        runner = null;
    }

    @Test
    public void testInitFramework() throws Exception {
        runner.initFramework();
        verify(runner.getFramework(), times(1)).init();
    }

    @Test
    public void testGetFrameworkFactory() throws Exception {
        FrameworkFactory factory = runner.getFrameworkFactory();
        assertTrue(factory instanceof MockFrameworkFactory);
    }
}
