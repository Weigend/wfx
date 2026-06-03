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
package io.softwareecg.wfx.windowmanager.impl;

import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.lookup.api.LookupStrategy;
import io.softwareecg.wfx.windowmanager.testutil.JavaFXThreadingRule;
import io.softwareecg.wfx.windowmanager.api.Position;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the root area.
 */
@RunWith(MockitoJUnitRunner.class)
public class RootAreaTest {
    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();

    @Mock
    private ViewArea firstChild;

    @Mock
    private DragNDropManager dragNDropManager;

    @Mock
    private MultiWindowManager windowManager;

    @Mock
    private LookupStrategy lookupStrategy;

    private RootArea rootArea;

    @Before
    public void setUp() {
        when(lookupStrategy.lookup(ViewContainerAreaFactory.class)).thenReturn(new ViewContainerAreaFactoryMockImpl());
        Lookup.init(lookupStrategy);
        when(dragNDropManager.getWindowManager()).thenReturn(windowManager);
        when(firstChild.getNode()).thenReturn(new Label("abc"));
        rootArea = new RootArea(dragNDropManager, false);
        rootArea.setFirstChild(firstChild);
    }

    @Test
    public void testAddDelegatesToFirstChild() {
        ViewStatus status = mock(ViewStatus.class);
        rootArea.add(status, Position.CENTER);
        verify(firstChild).add(status, Position.CENTER);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetSecondChildIsForbidden() {
        rootArea.setSecondChild(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSplitIsForbidden() {
        rootArea.split(null, null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetParentIsForbidden() {
        rootArea.setParent(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveOnNonClosingRootIsForbidden() {
        // RootArea created with closeStage=false rejects remove() to enforce
        // its single-child invariant.
        rootArea.remove(firstChild);
    }

    @Test
    public void testRemoveOnClosingRootClosesStage() throws ReflectiveOperationException {
        // RootArea with closeStage=true closes the containing Stage when its
        // (single) child is removed. Use a real Stage rather than reflectively
        // patching Scene.window — that hack relies on a private JavaFX field
        // and breaks across JavaFX/Java versions.
        FieldUtils.writeField(rootArea, "closeStage", true, true);
        Stage stage = new Stage();
        stage.setScene(new Scene(rootArea.getNode()));
        stage.show();
        assertThat(stage.isShowing(), is(true));

        rootArea.remove(firstChild);
        assertThat(stage.isShowing(), is(false));
    }
}
