// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 05.03.14 10:46
//      description:
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.windowmtg.api.TestApplication;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for platform ui core tests.
 * <p>
 * It starts the javafx application before each testclass and terminates it after the test.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({PlatformApplicationImplTest.class})
public class PlatformUITestSuite {
    @BeforeClass
    public static void setUp() {
        TestApplication.launchTest();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        TestApplication.stopTest();
    }
}
