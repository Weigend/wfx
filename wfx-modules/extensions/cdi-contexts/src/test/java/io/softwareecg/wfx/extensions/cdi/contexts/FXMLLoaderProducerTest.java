/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 Weigend AM
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.softwareecg.wfx.extensions.cdi.contexts;


import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.lookup.LookupStrategy;
import io.softwareecg.wfx.lookup.cdi.CDILookupStrategy;
import io.softwareecg.wfx.windowmtg.api.FXMLView;
import io.softwareecg.wfx.windowmtg.api.JavaFXThreadingRule;
import io.softwareecg.wfx.windowmtg.api.Position;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link io.softwareecg.wfx.extensions.cdi.contexts.FXMLLoaderProducer}.
 *
 */
@RunWith(Arquillian.class)
public class FXMLLoaderProducerTest {
    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();

    @Inject
    private LookupStrategy strategy;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(CDILookupStrategy.class)
                .addClass(TestController.class)
                .addClass(FXMLLoaderProducer.class)
                .addAsManifestResource("META-INF/beans.xml");
    }

    @Before
    public void setUp() throws Exception {
        Lookup.init(strategy);
    }

    @Test
    public void testLoadFxml() throws Exception {
        FXMLView<TestController> testControllerFXMLView = new FXMLView.Builder<TestController>()
                .withId("id")
                .withTitle("title")
                .withPos(Position.CENTER)
                .withFile(getClass().getResource("/io/softwareecg/wfx/extensions/cdi/contexts/test.fxml"))
                .build();

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
     */
    public static class TestController {
        @FXML
        private Label testLabel;

        private boolean postConstructCalled;

        /**
         * If {@link TestController#postConstructCalled} is equals true, assume that the class was initialized through
         * cdi.
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
