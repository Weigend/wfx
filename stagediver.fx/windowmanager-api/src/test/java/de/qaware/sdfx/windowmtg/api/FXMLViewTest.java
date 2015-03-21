// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 24.01.14 15:40
//      description: Tests the loading of a standard fxml view.
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.api;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.windowmtg.api.exceptions.ViewNotFoundException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javafx.fxml.*;
import javafx.scene.layout.*;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for loading of standard fxml views.
 */
public class FXMLViewTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestApplication.launchTest();
        LookupStrategy strategy = mock(LookupStrategy.class);
        when(strategy.lookup(FXMLLoader.class)).thenAnswer(invocationOnMock -> new FXMLLoader());
        Lookup.init(strategy);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        TestApplication.stopTest();
    }

    @Test(expected = ViewNotFoundException.class)
    public void testConstructorViewNotFound() throws Exception {
        new FXMLView<>("notFound", "notfound", Position.CENTER, "/notExisting.fxml", getClass().getClassLoader());
    }

    @Test
    public void testConstructorSuccessfull() throws Exception {
        View view = new FXMLView<>("example:1", "Example view", Position.CENTER,
                "de/qaware/sdfx/windowmtg/api/exampleView.fxml", getClass().getClassLoader()
        );
        assertThat(view, is(notNullValue()));
        assertThat(view.getRootNode(), is(instanceOf(BorderPane.class)));
        assertThat(view.getDefaultPosition(), is(Position.CENTER));
        assertThat(view.getViewAreaSize(), is(FXMLView.DEFAULT_VIEW_AREA_SIZE));
    }
}
