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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for loading of standard fxml views.
 */
public class FXMLViewTest {

    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();

    @BeforeClass
    public static void setUpClass() throws Exception {
        LookupStrategy strategy = mock(LookupStrategy.class);
        when(strategy.lookup(FXMLLoader.class)).thenAnswer(invocationOnMock -> new FXMLLoader());
        Lookup.init(strategy);
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

    @Test
    public void testConstructorMinimal() throws Exception {
        View view = new FXMLView<>("example:1", "Example view", Position.CENTER,
                "de/qaware/sdfx/windowmtg/api/exampleView.fxml");
        assertThat(view, is(notNullValue()));
        assertThat(view.getTitle(), is(equalTo("Example view")));
        assertThat(view.getViewId(), is(equalTo("example:1")));
        assertThat(view.getToolTipInfo(), is(nullValue()));
    }

    @Test
    public void testConstructorNonDefaultSize() throws Exception {
        FXMLView<TestController> view = new FXMLView<TestController>("example:1", "Example view", Position.CENTER,
                "de/qaware/sdfx/windowmtg/api/exampleView.fxml", 0.25);
        assertThat(view, is(notNullValue()));
        assertThat(view.getTitle(), is(equalTo("Example view")));
        assertThat(view.getViewId(), is(equalTo("example:1")));
        assertThat(view.getToolTipInfo(), is(nullValue()));
        assertThat(view.getViewAreaSize(), is(equalTo(0.25)));
        assertThat(view.getController(), is(notNullValue()));
        assertThat(view.getController().getLabel().getText(), is(equalTo("Content")));
    }

    public static class TestController {
        @FXML
        private Label label;

        public Label getLabel() {
            return label;
        }
    }
}
