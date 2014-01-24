// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 24.01.14 17:19
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.platform.impl;

import javafx.application.Preloader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test for the preloader notification service.
 *
 * Please note that the verification of the application mock is not possible because method
 * {@link javafx.application.Application#notifyPreloader(javafx.application.Preloader.PreloaderNotification)}
 * is final.
 */
@RunWith(MockitoJUnitRunner.class)
public class PreloaderNotificationServiceImplTest {

    @Mock
    private PlatformApplication application;

    @Mock
    private Bundle bundle;

    private PreloaderNotificationServiceImpl notificationService;

    @Before
    public void setUp() throws Exception {
        notificationService = new PreloaderNotificationServiceImpl();
    }

    @Test
    public void testSendNotificationWithQueue() throws Exception {
        assertThat(notificationService.sendNotification(bundle, "Message", 0.5), is(false));
        assertThat(notificationService.sendNotification(mock(Bundle.class), "Message", 1), is(false));
        notificationService.setApplication(application);
    }

    @Test
    public void testSendNotification() throws Exception {
        notificationService.setApplication(application);
        assertThat(notificationService.sendNotification(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START)), is(true));
    }
}
