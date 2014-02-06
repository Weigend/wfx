package de.qaware.sdfx.windowmtg.itest;


import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import de.qaware.sdfx.windowmtg.impl.WindowManagerImpl;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class InitialPositionUITest extends GuiTest {

    private WindowManager windowManager = new WindowManagerImpl();

    private View center = new TestView("Center", Position.CENTER);

    private View left = new TestView("Left", Position.LEFT);

    private View bottom = new TestView("Bottom", Position.BOTTOM);

    private View top = new TestView("Top", Position.TOP);

    @Before
    public void setUp() throws Exception {

        windowManager.register(center);
        windowManager.register(left);
        windowManager.register(bottom, center);
        windowManager.register(top, left);
        windowManager.init();
    }

    @Test
    public void testPositions() throws Exception {
        sleep(500);
        Parent center = find("#center");
        assertThat(center, notNullValue());
        Parent p = getLogicalParent(getLogicalParent(center));
        assertThat(find("#bottom", p), notNullValue());
        p=getLogicalParent(p);
        assertThat(find("#top", p), notNullValue());
        assertThat(find("#left", p), notNullValue());
    }

    private Parent getLogicalParent(Parent node) {
        Parent parent = node.getParent();
        while (parent != null) {
            if (parent instanceof SplitPane || parent instanceof TabPane) {
                return parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    @Override
    protected Parent getRootNode() {
        return windowManager.getRootPane();
    }

    private class TestView implements View {

        private String id;

        private Position position;

        public TestView(String id, Position position) {
            this.id = id;
            this.position = position;
        }

        @Override
        public String getViewId() {
            return id;
        }

        @Override
        public String getTitle() {
            return id;
        }

        @Override
        public String getToolTipInfo() {
            return id;
        }

        @Override
        public Position getDefaultPosition() {
            return position;
        }

        @Override
        public Parent getRootNode() {
            Label l = new Label("View ID: " + id);
            l.setId(id.toLowerCase());
            return l;
        }

        @Override
        public double getViewAreaSize() {
            return 0.5;
        }
    }
}
