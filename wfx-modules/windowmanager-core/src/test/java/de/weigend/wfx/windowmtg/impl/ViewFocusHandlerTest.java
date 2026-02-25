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
package de.weigend.wfx.windowmtg.impl;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.lookup.impl.ServiceLoaderLookupStrategy;
import de.weigend.wfx.windowmtg.api.Position;
import de.weigend.wfx.windowmtg.api.View;
import javafx.scene.Parent;
import javafx.scene.control.TabPane;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for the {@link ViewFocusHandler}.
 *
 */
//unWith(MockitoJUnitRunner.class)
public class ViewFocusHandlerTest {
   /*
    static {
        stage = GuiTestHelper.getStage();
        stage.setX(50);
        stage.setY(50);
        stage.setHeight(500);
        stage.setWidth(500);
    }*/

    private final WindowManagerImpl windowManager = new WindowManagerImpl();

    public ViewFocusHandlerTest() {
        ViewFocusHandler focusHandler = new ViewFocusHandler(windowManager);
        focusHandler.init();
    }

    private final View view1 = new TestView("Test1", Position.CENTER);
    private final View view2 = new TestView("Test2", Position.LEFT);

    @BeforeClass
    public static void setUpClass() throws Exception {
        ServiceLoaderLookupStrategy lookupStrategy = new ServiceLoaderLookupStrategy();
        ViewContainerAreaFactory factory = mock(ViewContainerAreaFactory.class);
        when(factory.getInstance(any(ViewArea.class), any(DragNDropManager.class)))
                .thenAnswer(i -> new TabAreaMock((ViewArea) i.getArguments()[0], (DragNDropManager) i.getArguments()[1]));
        when(factory.getInstance(any(DragNDropManager.class)))
                .thenAnswer(i -> new TabAreaMock((DragNDropManager) i.getArguments()[0]));
        lookupStrategy.init(ViewContainerAreaFactory.class, factory);
        Lookup.init(lookupStrategy);
    }

    @Before
    public void setUp() throws Exception {
        windowManager.register(view1);
        windowManager.register(view2, view1);
    }
/*
    @Test
    public void testSingleWindow() throws Exception {
        runInJavaFxThreadAndWait(windowManager::init);
        sleep(1000);
        click(view1.getRootNode(), MouseButton.PRIMARY);
        assertThat(windowManager.getFocusedView(), is(view1));
        click(view2.getRootNode(), MouseButton.PRIMARY);
        assertThat(windowManager.getFocusedView(), is(view2));
    }
*/
    //@Override
    protected Parent getRootNode() {
        return windowManager.getRootPane();
    }

    private static class TabAreaMock extends TabArea {
        private final TabPane node = new TabPane();

        protected TabAreaMock(ViewArea parent, DragNDropManager dragNDropManager) {
            super(parent, dragNDropManager);
            node.setUserData(this);
        }

        protected TabAreaMock(DragNDropManager dragNDropManager) {
            super(dragNDropManager);
            node.setUserData(this);
        }

        @Override
        public void add(ViewStatus view, Position position) {
            if (position != Position.CENTER) {
                super.add(view, position);
                return;
            }
            view.setArea(this);
            view.setPosition(position);
            node.getTabs().add(view.getTab());
        }

        @Override
        public Parent getNode() {
            return node;
        }
    }
}
