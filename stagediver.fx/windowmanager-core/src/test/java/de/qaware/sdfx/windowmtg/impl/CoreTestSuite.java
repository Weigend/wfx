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

package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.windowmtg.api.TestApplication;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for windowmanager core tests.
 * <p/>
 * It starts the javafx application before each testclass and terminates it after the test.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({RootAreaTest.class, TabAreaTest.class, ViewAreaTest.class, ViewStatusTest.class})
public class CoreTestSuite {
    @BeforeClass
    public static void setUp() {
        TestApplication.launchTest();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        TestApplication.stopTest();
    }
}
