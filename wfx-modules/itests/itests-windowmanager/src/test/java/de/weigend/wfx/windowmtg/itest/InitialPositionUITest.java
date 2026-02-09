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
package de.weigend.wfx.windowmtg.itest;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.lookup.impl.ServiceLoaderLookupStrategy;
import de.weigend.wfx.windowmtg.api.GuiTestHelper;
import de.weigend.wfx.windowmtg.api.Position;
import de.weigend.wfx.windowmtg.api.View;
import de.weigend.wfx.windowmtg.api.WindowManager;
import de.weigend.wfx.windowmtg.impl.TestView;
import de.weigend.wfx.windowmtg.impl.WindowManagerImpl;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration test to test the initial positions of views within the window manager.
 *
 * @author Software-EKG Team
 */
public class InitialPositionUITest extends GuiTest {
    static {
        stage = GuiTestHelper.getStage();
        stage.setHeight(500);
        stage.setWidth(500);
    }

    private WindowManager windowManager = new WindowManagerImpl();

    private View center = new TestView("Center", Position.CENTER);

    private View left = new TestView("Left", Position.LEFT);

    private View bottom = new TestView("Bottom", Position.BOTTOM);

    private View top = new TestView("Top", Position.TOP);

    @BeforeClass
    public static void setUpClass() throws Exception {
        Lookup.init(new ServiceLoaderLookupStrategy());
    }

    @Before
    public void setUp() throws Exception {
        windowManager.register(center);
        windowManager.register(left);
        windowManager.register(bottom, center);
        windowManager.register(top, left);
    }

    @Test
    public void testPositions() throws Exception {
        windowManager.init();
        sleep(1000);
        Parent center = find("#center");
        assertThat(center, notNullValue());
        Parent p = getLogicalParent(getLogicalParent(center));
        assertThat(find("#bottom", p), notNullValue());
        p = getLogicalParent(p);
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
}
