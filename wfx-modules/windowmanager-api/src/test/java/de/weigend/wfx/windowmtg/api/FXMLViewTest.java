/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
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
package de.weigend.wfx.windowmtg.api;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.lookup.LookupStrategy;
import de.weigend.wfx.windowmtg.api.exceptions.ViewNotFoundException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for loading of standard fxml views with {@link FXMLView}.
 *
 * @author christian.fritz
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
                "de/weigend/wfx/windowmtg/api/exampleView.fxml", getClass().getClassLoader()
        );
        assertThat(view, is(notNullValue()));
        assertThat(view.getRootNode(), is(instanceOf(BorderPane.class)));
        assertThat(view.getDefaultPosition(), is(Position.CENTER));
        assertThat(view.getViewAreaSize(), is(FXMLView.DEFAULT_VIEW_AREA_SIZE));
    }

    @Test
    public void testConstructorMinimal() throws Exception {
        View view = new FXMLView<>("example:1", "Example view", Position.CENTER,
                "de/weigend/wfx/windowmtg/api/exampleView.fxml");
        assertThat(view, is(notNullValue()));
        assertThat(view.getTitle(), is(equalTo("Example view")));
        assertThat(view.getViewId(), is(equalTo("example:1")));
        assertThat(view.getToolTipInfo(), is(nullValue()));
    }

    @Test
    public void testConstructorNonDefaultSize() throws Exception {
        FXMLView<TestController> view = new FXMLView<>("example:1", "Example view", Position.CENTER,
                "de/weigend/wfx/windowmtg/api/exampleView.fxml", 0.25);
        assertThat(view, is(notNullValue()));
        assertThat(view.getTitle(), is(equalTo("Example view")));
        assertThat(view.getViewId(), is(equalTo("example:1")));
        assertThat(view.getToolTipInfo(), is(nullValue()));
        assertThat(view.getViewAreaSize(), is(equalTo(0.25)));
        assertThat(view.getController(), is(notNullValue()));
        assertThat(view.getController().getLabel().getText(), is(equalTo("Content")));
    }

    @Test
    public void testBuilder() throws Exception {
        FXMLView<TestController> view = new FXMLView.Builder<TestController>().withId("example:1")
                .withTitle("Example view")
                .withClassLoader(getClass().getClassLoader())
                .withFile("de/weigend/wfx/windowmtg/api/exampleView.fxml")
                .withPos(Position.CENTER)
                .withViewImage("de/weigend/wfx/windowmtg/api/test-icon.png")
                .withViewAreaSize(0.25)
                .build();

        assertThat(view, is(notNullValue()));
        assertThat(view.getTitle(), is(equalTo("Example view")));
        assertThat(view.getViewId(), is(equalTo("example:1")));
        assertThat(view.getToolTipInfo(), is(nullValue()));
        assertThat(view.getViewAreaSize(), is(equalTo(0.25)));
        assertThat(view.getController(), is(notNullValue()));
        assertThat(view.getViewImagePath(), is(notNullValue()));
        assertThat(view.getController().getLabel().getText(), is(equalTo("Content")));
    }

    @Test
    public void testBuilderWithoutClassLoader() throws Exception {
        FXMLView<TestController> view = new FXMLView.Builder<TestController>().withId("example:1")
                .withTitle("Example view")
                .withFile(getClass().getResource("exampleView.fxml"))
                .withPos(Position.CENTER)
                .withViewImage(getClass().getResource("test-icon.png"))
                .withToolTipInfo("Tooltip")
                .build();

        assertThat(view, is(notNullValue()));
        assertThat(view.getToolTipInfo(), is(equalTo("Tooltip")));
        assertThat(view.getController(), is(notNullValue()));
        assertThat(view.getController().getLabel().getText(), is(equalTo("Content")));
        assertThat(view.getViewImagePath(), is(notNullValue()));
    }

    public static class TestController {
        @FXML
        private Label label;

        public Label getLabel() {
            return label;
        }
    }
}
