package de.qaware.sdfx.windowmtg.itest;


import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import de.qaware.sdfx.windowmtg.impl.WindowManagerImpl;
import org.junit.Before;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import javafx.scene.*;
import javafx.scene.control.*;

import static org.loadui.testfx.Assertions.assertNodeExists;
import static org.loadui.testfx.controls.Commons.hasText;

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
        assertNodeExists(hasText("Center"));
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
            return new Label("View ID: " + id);
        }

        @Override
        public double getViewAreaSize() {
            return 0.5;
        }
    }
}
