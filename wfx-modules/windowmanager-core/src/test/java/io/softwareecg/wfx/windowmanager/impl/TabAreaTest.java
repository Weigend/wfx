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
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link TabArea}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TabAreaTest {
    @ClassRule
    public static JavaFXThreadingRule threadingRule = new JavaFXThreadingRule();

    @Mock
    private DragNDropManager dragNDropManager;

    @Mock
    private MultiWindowManager windowManager;

    @Mock
    private ViewArea parent;

    @Mock
    private LookupStrategy lookupStrategy;

    private RootArea rootArea;
    private TabArea tabArea;

    @Before
    public void setUp() {
        when(lookupStrategy.lookup(ViewContainerAreaFactory.class)).thenReturn(new ViewContainerAreaFactoryMockImpl());
        Lookup.init(lookupStrategy);

        when(dragNDropManager.getWindowManager()).thenReturn(windowManager);
        rootArea = new RootArea(dragNDropManager, true);
        when(parent.getParent()).thenReturn(rootArea);
        tabArea = new TabArea(parent, dragNDropManager);
    }

    @Test
    public void testRemoveViewNotAssigned() {
        ViewStatus view = mock(ViewStatus.class);
        tabArea.remove(view);
        // remove() is a no-op when the view is unknown — no setArea/null call
        verify(view, never()).setArea(null);
    }

    @Test
    public void testHandleEmptyOnNonEmptyTab() {
        // After adding a view in CENTER, handleEmpty() must report the area as
        // non-empty and not remove it from the parent.
        ViewStatus status = mock(ViewStatus.class);
        tabArea.add(status, Position.CENTER);
        assertThat(tabArea.handleEmpty(), is(false));
    }

    @Test
    public void testAddNonCenterSplitsParent() {
        // Adding a non-CENTER view triggers ViewArea.add() which splits the
        // parent. The new wrapper area replaces this TabArea and contains
        // it as one of its children.
        ViewStatus status = mock(ViewStatus.class);
        when(status.getView()).thenReturn(new TestView("dummy", Position.TOP));
        // After the split, ViewArea.add() requests a layout via view.getArea().getNode().
        // The factory mock returns a Mockito stub for the new container, so we wire
        // the status to a real TabArea (with a real getNode()) to keep that path alive.
        when(status.getArea()).thenReturn(tabArea);
        ArgumentCaptor<ViewArea> captor = ArgumentCaptor.forClass(ViewArea.class);

        tabArea.add(status, Position.TOP);

        verify(parent).replace(eq(tabArea), captor.capture());
        // For TOP, the new view goes first and the existing tabArea second.
        assertThat(captor.getValue().getSecondChild(), is(sameInstance((ViewArea) tabArea)));
        assertThat(captor.getValue().getParent(), is(sameInstance(parent)));
    }
}
