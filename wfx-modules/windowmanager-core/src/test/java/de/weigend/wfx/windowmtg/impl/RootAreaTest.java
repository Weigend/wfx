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
import de.weigend.wfx.windowmtg.api.JavaFXThreadingRule;
import de.weigend.wfx.windowmtg.api.Position;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

/**
 * Test the root area.
 *
 * @author christian.fritz
 */
//@RunWith(MockitoJUnitRunner.class)
public class RootAreaTest {
//    @ClassRule
//    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();
//    private RootArea rootArea;
//
//    @Mock
//    private ViewArea firstChild;
//
//    @Mock
//    private DragNDropManager dragNDropManager;
//
//    @Mock
//    private MultiWindowManager windowManager;
//
//    @Before
//    public void setUp() throws Exception {
//        Lookup.init(new ServiceLoaderLookupStrategy());
//        when(dragNDropManager.getWindowManager()).thenReturn(windowManager);
//        when(firstChild.getNode()).thenReturn(new Label("abc"));
//        rootArea = new RootArea(dragNDropManager, false);
//        rootArea.setFirstChild(firstChild);
//    }
//
//    @Test
//    public void testSetFirstChild() throws Exception {
//
//    }
//
//
//    @Test(expected = UnsupportedOperationException.class)
//    public void testSetSecondChild() throws Exception {
//        rootArea.setSecondChild(null);
//    }
//
//    @Test(expected = UnsupportedOperationException.class)
//    public void testSplit() throws Exception {
//        rootArea.split(null, null, null);
//    }
//
//    @Test
//    public void testAdd() throws Exception {
//        ViewStatus status = mock(ViewStatus.class);
//        rootArea.add(status, Position.CENTER);
//        verify(firstChild).add(status, Position.CENTER);
//    }
//
//    @Test(expected = UnsupportedOperationException.class)
//    public void testRemove() throws Exception {
//        rootArea.remove(firstChild);
//    }
//
//    @Test(expected = UnsupportedOperationException.class)
//    public void testRemoveCloseNoClose() throws Exception {
//        rootArea.remove(firstChild);
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    public void testRemoveArea() throws Exception {
//        FieldUtils.writeField(rootArea, "closeStage", true, true);
//        Scene scene = new Scene(rootArea.getNode());
//        Stage stage = mock(Stage.class);
//        scene.windowProperty();
//        ReadOnlyObjectWrapper<Stage> stageProperty = (ReadOnlyObjectWrapper<Stage>) FieldUtils.readField(scene, "window", true);
//        FieldUtils.writeField(stageProperty, "value", stage, true);
//
//        rootArea.remove(firstChild);
//        verify(stage).close();
//    }
//
//    @Test(expected = UnsupportedOperationException.class)
//    public void testSetParent() throws Exception {
//        rootArea.setParent(null);
//    }
}
