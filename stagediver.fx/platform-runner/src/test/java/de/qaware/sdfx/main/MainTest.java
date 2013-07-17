package de.qaware.sdfx.main;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.FrameworkFactory;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class MainTest {

    private Main runner;

    @Before
    public void setUp() throws Exception {
        runner = new Main();
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
        runner = new Main();
        runner.initFramework();
        verify(runner.getFramework(), times(1)).init();
    }

    @Test
    public void testGetFrameworkFactory() throws Exception {
        FrameworkFactory factory = runner.getFrameworkFactory();
        assertTrue(factory instanceof MockFrameworkFactory);
    }
}
