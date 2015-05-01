/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
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
package de.qaware.sdfx.windowmtg.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy;
import de.qaware.sdfx.windowmtg.api.GuiTestHelper;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loadui.testfx.GuiTest;
import org.mockito.runners.MockitoJUnitRunner;

import javafx.scene.*;
import javafx.scene.input.*;

import static de.qaware.sdfx.windowmtg.api.GuiTestHelper.runInJavaFxThreadAndWait;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewFocusHandlerTest extends GuiTest {
    static {
        stage = GuiTestHelper.getStage();
        stage.setX(50);
        stage.setY(50);
        stage.setHeight(500);
        stage.setWidth(500);
    }

    private WindowManagerImpl windowManager = new WindowManagerImpl();
    private View view1 = new TestView("Test1", Position.CENTER);
    private View view2 = new TestView("Test2", Position.LEFT);

    @BeforeClass
    public static void setUpClass() throws Exception {
        Lookup.init(new ServiceLoaderLookupStrategy());
    }

    @Before
    public void setUp() throws Exception {
        new ViewFocusHandler(windowManager);

        windowManager.register(view1);
        windowManager.register(view2, view1);
    }

    @Test
    public void testSingleWindow() throws Exception {
        runInJavaFxThreadAndWait(windowManager::init);
        click(view1.getRootNode(), MouseButton.PRIMARY);
        assertThat(windowManager.getFocusedView(), is(view1));
        click(view2.getRootNode(), MouseButton.PRIMARY);
        assertThat(windowManager.getFocusedView(), is(view2));
    }

    @Override
    protected Parent getRootNode() {
        return windowManager.getRootPane();
    }
}
