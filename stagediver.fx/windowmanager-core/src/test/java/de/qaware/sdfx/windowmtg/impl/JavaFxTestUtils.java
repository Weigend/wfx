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

import com.sun.javafx.tk.Toolkit;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import org.mockito.internal.util.reflection.Whitebox;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

/**
 * Some utility methods to test java fx.
 *
 * @author christian.fritz
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

        Object objectWrapper = Whitebox.getInternalState(target, property);
        Whitebox.setInternalState(objectWrapper, "value", value);
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
        when(view.getRootNode()).thenReturn(mock(Parent.class));
        ViewStatus status = new ViewStatus(view);
        status.setArea(mock(TabArea.class));
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
        Constructor<Dragboard> constructor = Dragboard.class.getDeclaredConstructor(com.sun.javafx.tk.TKClipboard.class);
        constructor.setAccessible(true);
        Dragboard dragboard = constructor.newInstance(Toolkit.getToolkit().createLocalClipboard());

        ClipboardContent content = new ClipboardContent();
        content.put(dateFormat, viewId);

        dragboard.setContent(content);
        return dragboard;
    }
}
