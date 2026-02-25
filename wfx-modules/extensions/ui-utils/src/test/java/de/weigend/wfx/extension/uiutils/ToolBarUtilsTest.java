/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 Weigend AM
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
package de.weigend.wfx.extension.uiutils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.weigend.wfx.extension.uiutils.ToolBarUtils.addButtonAfter;
import static de.weigend.wfx.extension.uiutils.ToolBarUtils.findIndex;
import static de.weigend.wfx.windowmtg.api.GuiTestHelper.getStage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link ToolBarUtils}.
 *
 */
public class ToolBarUtilsTest {
    static {
        getStage();
    }

    @Test
    public void testAddButtonAfter() throws Exception {
        List<Node> nodes = new ArrayList<>(Arrays.asList(createNode("1"), createNode("2"), createNode("3")));
        Button button = addButtonAfter(nodes, "1", "btn", "btn", ToolBarUtilsTest.class.getResource("test.png"), event -> {
        });

        assertThat(nodes.get(1), is(sameInstance(button)));
    }

    @Test
    public void testFindIndex() throws Exception {
        List<Node> nodes = Arrays.asList(createNode("1"), createNode("2"), createNode("3"));
        assertThat(findIndex(nodes, "0"), is(equalTo(2)));
        assertThat(findIndex(nodes, "3"), is(equalTo(2)));
        assertThat(findIndex(nodes, "2"), is(equalTo(1)));
        assertThat(findIndex(nodes, "1"), is(equalTo(0)));
    }

    private static Node createNode(String id) {
        Label label = new Label();
        label.setId(id);
        return label;
    }
}