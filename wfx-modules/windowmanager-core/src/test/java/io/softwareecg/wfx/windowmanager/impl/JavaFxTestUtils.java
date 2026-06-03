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

import io.softwareecg.wfx.windowmanager.api.Position;
import io.softwareecg.wfx.windowmanager.api.View;
import io.softwareecg.wfx.windowmanager.api.ViewKind;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.stage.Stage;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

/**
 * Some utility methods to test java fx.
 *
 */
public final class JavaFxTestUtils {

    private JavaFxTestUtils() {
    }

    /**
     * Mock a read only property.
     *
     * @param target   The target for the mocked property.
     * @param property The property name to mock.
     * @param value    The returned value.
     * @param <T>      The type of the returned value.
     */
    @SuppressWarnings("unchecked")
    public static <T> void mockReadOnlyProperty(Object target, String property, T value) throws ReflectiveOperationException {
        Method getPropertyMethod = target.getClass().getMethod(property + "Property");
        getPropertyMethod.setAccessible(true);
        getPropertyMethod.invoke(target);

        Field[] fields = FieldUtils.getAllFields(target.getClass());

        Field lookupField = null;

        for (Field field : fields) {
            if (field.getName().equals(property)) {
                lookupField = field;
                break;
            }
        }

        if (lookupField != null) {
            Object objectWrapper = FieldUtils.readField(lookupField, target, true);
            FieldUtils.writeField(objectWrapper, "value", value, true);
        }


    }

    /**
     * Mock the stage for a {@link ViewArea}.
     *
     * @param area Mock the stage for this area.
     * @return The mocked stage.
     */
    @SuppressWarnings("unchecked")
    public static Stage mockStageForArea(ViewArea area) throws ReflectiveOperationException {
        Parent parent = new Label();
        Scene scene = new Scene(parent);
        when(area.getNode()).thenReturn(parent);

        Stage stage = mock(Stage.class);
        mockReadOnlyProperty(scene, "window", stage);
        return stage;
    }

    /**
     * Mock a {@link View} with corresponding {@link ViewStatus}.
     *
     * @param id    The id for the new view.
     * @param title The title for the new view.
     * @return The mocked {@link ViewStatus}.
     */
    public static ViewStatus mockView(String id, String title) {
        View view = mock(View.class);
        when(view.getViewId()).thenReturn(id);
        when(view.getTitle()).thenReturn(title);
        when(view.getDefaultPosition()).thenReturn(Position.CENTER);
        // Mockito returns null for unstubbed default methods, so explicitly
        // stub the kind to TOOL — that mirrors the production default and
        // exercises the tool-registry tracking in WindowManagerImpl tests.
        when(view.getKind()).thenReturn(ViewKind.TOOL);
        // Use a real Pane: Mockito's inline mockmaker cannot mock JavaFX Parent
        // because of its @IDProperty annotation across module boundaries.
        when(view.getRootNode()).thenReturn(new Pane());
        ViewStatus status = new ViewStatus(view);
        // Provide a TabArea with a parent + node so that ViewStatus.setDividerPositions()
        // can run without NPE. The parent's node is a non-SplitPane (Pane), so the
        // method early-returns — exactly what we want for unit tests that only
        // care about the surrounding registration logic.
        TabArea area = mock(TabArea.class);
        ViewArea parent = mock(ViewArea.class);
        when(area.getParent()).thenReturn(parent);
        when(parent.getNode()).thenReturn(new Pane());
        // When tests register a new view via this area, simulate the real
        // TabArea.add() side effect of attaching the area to the new ViewStatus.
        // Otherwise, the WindowManagerImpl's subsequent setDividerPositions()
        // sweep NPEs on the freshly created ViewStatus.
        Mockito.doAnswer(invocation -> {
            ViewStatus added = invocation.getArgument(0);
            added.setArea(area);
            return null;
        }).when(area).add(Mockito.any(ViewStatus.class), Mockito.any(Position.class));
        status.setArea(area);
        return spy(status);
    }

    /**
     * Mock the drag board.
     *
     * @param dateFormat the data format of the default content.
     * @param viewId     the default content.
     * @return the mocked dragboard with given content.
     */
    public static Dragboard mockDragboard(DataFormat dateFormat, String viewId) throws ReflectiveOperationException {

        Dragboard dragboardMock = Mockito.mock(Dragboard.class);

        ClipboardContent content = new ClipboardContent();
        content.put(dateFormat, viewId);

        doReturn(content).when(dragboardMock).getContent(any());

        return dragboardMock;


    }
}
