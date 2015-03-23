package de.qaware.sdfx.windowmtg.impl;


import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.cdi.CDILookupStrategy;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.PostConstruct;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link de.qaware.sdfx.windowmtg.impl.FXMLLoaderProducer}.
 *
 * @author christian.fritz
 */
public class FXMLLoaderProducerTest {

    @BeforeClass
    public static void setUp() throws Exception {
        CDILookupStrategy.initLookup();
    }

    @Test
    public void testLoadFxml() throws Exception {
        FXMLView<TestController> testControllerFXMLView = new FXMLView<>("id", "title", Position.CENTER,
                "de/qaware/sdfx/windowmtg/impl/test.fxml", getClass().getClassLoader());

        assertThat(testControllerFXMLView.getController().getTestLabel().getText(), is(equalTo("test")));
        assertThat(testControllerFXMLView.getController().isPostConstructCalled(), is(true));
    }

    @Test
    public void testFxmlLoaderProducer() throws Exception {
        TestController controller = (TestController) Lookup.lookup(FXMLLoader.class).getControllerFactory().call(TestController.class);
        assertThat(controller, is(notNullValue()));
        assertThat(controller.isPostConstructCalled(), is(true));
    }

    /**
     * Controller for testing the correct initialization
     *
     * @author christian.fritz
     */
    public static class TestController {
        @FXML
        private Label testLabel;

        private boolean postConstructCalled;

        /**
         * If {@link TestController#postConstructCalled} is equals true, assume that the class was initialized through cdi.
         */
        @PostConstruct
        public void init() {
            postConstructCalled = true;
        }

        public Label getTestLabel() {
            return testLabel;
        }

        public boolean isPostConstructCalled() {
            return postConstructCalled;
        }
    }
}