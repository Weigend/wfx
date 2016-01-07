package de.qaware.sdfx.extension.uiutils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.qaware.sdfx.extension.uiutils.ToolBarUtils.addButtonAfter;
import static de.qaware.sdfx.extension.uiutils.ToolBarUtils.findIndex;
import static de.qaware.sdfx.windowmtg.api.GuiTestHelper.getStage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit test for the {@link ToolBarUtils}.
 *
 * @author christian.fritz
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