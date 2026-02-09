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
package de.weigend.wfx.windowmtg.api;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.windowmtg.api.exceptions.ViewNotFoundException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * This is the default implementation of a window management view. It loads the view from an fxml file and defines the
 * other needed values.
 *
 * @param <C> Defines the type of the controller class.
 * @author Software-EKG Team
 */
public class FXMLView<C> implements View {

    public static final double DEFAULT_VIEW_AREA_SIZE = 0.5;
    private final String id;
    private final String title;
    private final Position defaultPosition;
    private final Parent rootPane;
    private final String toolTipInfo;
    private final double viewAreaSize;
    private final C controller;
    private final URL viewImagePath;

    private FXMLView(String id, String title, Position defaultPosition, Parent rootPane, String toolTipInfo, double viewAreaSize, C controller, URL viewImagePath) {
        this.id = id;
        this.title = title;
        this.defaultPosition = defaultPosition;
        this.rootPane = rootPane;
        this.toolTipInfo = toolTipInfo;
        this.viewAreaSize = viewAreaSize;
        this.controller = controller;
        this.viewImagePath = viewImagePath;
    }

    @Override
    public String getViewId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getToolTipInfo() {
        return toolTipInfo;
    }

    @Override
    public Position getDefaultPosition() {
        return defaultPosition;
    }

    @Override
    public Parent getRootNode() {
        return rootPane;
    }

    /**
     * Get the view area size. This will be a number between 0 and 1 which defines the percentage space of this view
     * within the surrounding area.
     *
     * @return The view area size.
     */
    @Override
    public double getViewAreaSize() {
        return this.viewAreaSize;
    }

    /**
     * Get the controller instance.
     *
     * @return The controller instance.
     */
    public C getController() {
        return controller;
    }

    @Override
    public URL getViewImagePath() {
        return viewImagePath;
    }

    @Override
    public String toString() {
        return "FXMLView{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", defaultPosition=" + defaultPosition +
                ", viewAreaSize=" + viewAreaSize +
                ", controller=" + controller +
                '}';
    }

    /**
     * Builder for new FXML views.
     *
     * @param <C> The type of the controller.
     */
    @SuppressWarnings({"findbugs:CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE", "PMD.CloneMethodMustImplementCloneable"})
    public static class Builder<C> {
        private String id;
        private String title;
        private Position pos;
        private URL file;
        private String toolTipInfo;
        private double viewAreaSize = DEFAULT_VIEW_AREA_SIZE;
        private ClassLoader classLoader;
        private URL viewImage;
        private Parent rootPane;
        private C controller;

        /**
         * Set the builder value "id"
         *
         * @param id the id
         * @return fluent builder interface
         */
        public Builder<C> withId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Set the builder value "title"
         *
         * @param title the title
         * @return fluent builder interface
         */
        public Builder<C> withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the builder value "pos"
         *
         * @param pos the position
         * @return fluent builder interface
         */
        public Builder<C> withPos(Position pos) {
            this.pos = pos;
            return this;
        }

        /**
         * Set the builder value "file"
         *
         * @param file the file
         * @return fluent builder interface
         */
        public Builder<C> withFile(URL file) {
            this.file = file;
            return this;
        }

        /**
         * Set the builder value "file"
         *
         * @param file the file
         * @return fluent builder interface
         */
        public Builder<C> withFile(String file) {
            Objects.requireNonNull(classLoader);
            this.file = classLoader.getResource(file);
            return this;
        }

        /**
         * Set the builder value "toolTipInfo"
         *
         * @param toolTipInfo the tool tip info
         * @return fluent builder interface
         */
        public Builder<C> withToolTipInfo(String toolTipInfo) {
            this.toolTipInfo = toolTipInfo;
            return this;
        }

        /**
         * Set the builder value "viewAreaSize"
         *
         * @param viewAreaSize the view area size
         * @return fluent builder interface
         */
        public Builder<C> withViewAreaSize(double viewAreaSize) {
            this.viewAreaSize = viewAreaSize;
            return this;
        }

        /**
         * Set the builder value "classLoader"
         *
         * @param classLoader the classloader
         * @return fluent builder interface
         */
        public Builder<C> withClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        /**
         * Set the builder value "viewImagePath"
         *
         * @param viewImage the image
         * @return fluent builder interface
         */
        public Builder<C> withViewImage(URL viewImage) {
            this.viewImage = viewImage;
            return this;
        }

        /**
         * Set the builder value "viewImagePath"
         *
         * @param viewImage the image
         * @return fluent builder interface
         */
        public Builder<C> withViewImage(String viewImage) {
            Objects.requireNonNull(classLoader);
            this.viewImage = classLoader.getResource(viewImage);
            return this;
        }

        /**
         * Set the builder value "rootPane"
         *
         * @param rootPane the root pane
         * @return fluent builder interface
         */
        public Builder<C> withRootPane(Parent rootPane) {
            // Objects.requireNonNull(rootPane);
            this.rootPane = rootPane;
            return this;
        }

        /**
         * Set the builder value "controller"
         *
         * @param controller the controller
         * @return fluent builder interface
         */
        public Builder<C> withController(C controller) {
            // Objects.requireNonNull(controller);
            this.controller = controller;
            return this;
        }

        /**
         * Build the real {@link FXMLView}.
         *
         * @return The new initialized view.
         * @throws IOException In case of the fxml can not be read.
         */
        public FXMLView<C> build() throws IOException {
            Objects.requireNonNull(id, "A view must have an unique id");
            Objects.requireNonNull(title, "A view must have a title");
            Objects.requireNonNull(pos, "The initial position must be set");
            Objects.requireNonNull(file, "Can not initialize a FXMLView without a FXML file.");

            FXMLLoader loader = Lookup.lookup(FXMLLoader.class);
            loader.setLocation(file);
            if (classLoader != null) {
                loader.setClassLoader(classLoader);
            }
            rootPane = loader.load();
            controller = loader.getController();


            return new FXMLView<>(id, title, pos, rootPane, toolTipInfo, viewAreaSize, controller, viewImage);
        }

        /**
         * Clone to real {@link FXMLView}.
         *
         * @return The new initialized view.
         */
        @SuppressWarnings({"findbugs:CN_IDIOM_NO_SUPER_CALL", "PMD.CloneThrowsCloneNotSupportedException"})
        public FXMLView<C> clone() {
            Objects.requireNonNull(id, "A view must have an unique id");
            Objects.requireNonNull(title, "A view must have a title");
            Objects.requireNonNull(pos, "The initial position must be set");
            // Objects.requireNonNull(controller, "Can not initialize a FXMLView without a controller.");

            return new FXMLView<>(id, title, pos, rootPane, toolTipInfo, viewAreaSize, controller, viewImage);
        }
    }
}
