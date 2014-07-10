// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 10.07.14 08:55
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.platform.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for platform activator.
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class PlatformActivatorTest {

    @Mock
    private BundleContext context;

    @Mock
    private Framework frameworkBundle;

    private PlatformActivator activator;

    @Before
    public void setUp() throws Exception {
        when(context.getBundle(0)).thenReturn(frameworkBundle);
        activator = new PlatformActivator();
    }

    @Test
    public void testStop() throws Exception {
        activator.stop(context);
        verify(frameworkBundle).stop();
    }
}