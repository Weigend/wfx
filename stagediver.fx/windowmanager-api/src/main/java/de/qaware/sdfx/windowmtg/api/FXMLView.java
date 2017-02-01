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
package de.qaware.sdfx.windowmtg.api;

import com.google.common.base.Preconditions;
import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.exceptions.ViewNotFoundException;
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
 * @author christian.fritz
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

    /**
     * Get a new view with the specified values.
     * <p>
     * The resulted view will not have a tooltip.
     *
     * @param id    The view id.
     * @param title The view title.
     * @param pos   The initial position of the view.
     * @param file  The path to the fxml file.
     * @throws IOException           In case of the view can not be loaded.
     * @throws ViewNotFoundException In case of the view can not be found.
     * @deprecated Use {@link FXMLView.Builder} now
     */
    @Deprecated
    public FXMLView(String id, String title, Position pos, String file) throws IOException {
        this(id, title, pos, file, FXMLView.class.getClassLoader());
    }

    /**
     * Get a new view with the specified values.
     * <p>
     * The resulted view will not have a tooltip.
     *
     * @param id          The view id.
     * @param title       The view title.
     * @param pos         The initial position of the view.
     * @param file        The path to the fxml file.
     * @param classLoader The class loader to resolve the fxml file and its controller.
     * @throws IOException           In case of the view can not be loaded.
     * @throws ViewNotFoundException In case of the view can not be found.
     * @deprecated Use {@link FXMLView.Builder} now
     */
    @Deprecated
    public FXMLView(String id, String title, Position pos, String file, ClassLoader classLoader) throws IOException {
        this(id, title, pos, file, null, classLoader);
    }

    /**
     * Get a new view with the specified values.
     * <p>
     * The resulted view will not have a tooltip.
     *
     * @param id           The view id.
     * @param title        The view title.
     * @param pos          The initial position of the view.
     * @param file         The path to the fxml file.
     * @param viewAreaSize The view area size. See {@link de.qaware.sdfx.windowmtg.api.View#getViewAreaSize()}.
     * @throws IOException           In case of the view can not be loaded.
     * @throws ViewNotFoundException In case of the view can not be found.
     * @deprecated Use {@link FXMLView.Builder} now
     */
    @Deprecated
    public FXMLView(String id, String title, Position pos, String file, double viewAreaSize) throws IOException {
        this(id, title, pos, file, viewAreaSize, FXMLView.class.getClassLoader());
    }

    /**
     * Get a new view with the specified values.
     * <p>
     * The resulted view will not have a tooltip.
     *
     * @param id           The view id.
     * @param title        The view title.
     * @param pos          The initial position of the view.
     * @param file         The path to the fxml file.
     * @param viewAreaSize The view area size. See {@link de.qaware.sdfx.windowmtg.api.View#getViewAreaSize()}.
     * @param classLoader  The class loader to resolve the fxml file and its controller.
     * @throws IOException           In case of the view can not be loaded.
     * @throws ViewNotFoundException In case of the view can not be found.
     * @deprecated Use {@link FXMLView.Builder} now
     */
    @Deprecated
    public FXMLView(String id, String title, Position pos, String file, double viewAreaSize, ClassLoader classLoader) throws IOException {
        this(id, title, pos, file, null, viewAreaSize, classLoader);
    }

    /**
     * Get a new view with the specified values.
     * <p>
     * The view will show a tooltip info on mouse over.
     *
     * @param id          The view id.
     * @param title       The view title.
     * @param pos         The initial position of the view.
     * @param file        The path to the fxml file.
     * @param toolTipInfo The tooltip info.
     * @param classLoader The class loader to resolve the fxml file and its controller.
     * @throws IOException           In case of the view can not be loaded.
     * @throws ViewNotFoundException In case of the view can not be found.
     * @deprecated Use {@link FXMLView.Builder} now
     */
    @Deprecated
    public FXMLView(String id, String title, Position pos, String file, String toolTipInfo, ClassLoader classLoader)
            throws IOException {
        this(id, title, pos, file, toolTipInfo, DEFAULT_VIEW_AREA_SIZE, classLoader);
    }


    /**
     * Get a new view with the specified values.
     * <p>
     * The view will show a tooltip info on mouse over.
     *
     * @param id           The view id.
     * @param title        The view title.
     * @param pos          The initial position of the view.
     * @param file         The path to the fxml file.
     * @param toolTipInfo  The tooltip info.
     * @param viewAreaSize The view area size. See {@link de.qaware.sdfx.windowmtg.api.View#getViewAreaSize()}.
     * @param classLoader  The class loader to resolve the fxml file and its controller.
     * @throws IOException           In case of the view can not be loaded.
     * @throws ViewNotFoundException In case of the view can not be found.
     * @deprecated Use {@link FXMLView.Builder} now
     */
    @Deprecated
    public FXMLView(String id, String title, Position pos, String file, String toolTipInfo, double viewAreaSize, ClassLoader classLoader)
            throws IOException {

        Preconditions.checkNotNull(classLoader);
        this.id = id;
        this.title = title;
        this.defaultPosition = pos;
        this.toolTipInfo = toolTipInfo;
        this.viewAreaSize = viewAreaSize;

        URL location = classLoader.getResource(file);
        if (location == null) {
            throw new ViewNotFoundException("Can not find view '" + file + "'. Please check the bundle exports and imports.");
        }
        FXMLLoader loader = Lookup.lookup(FXMLLoader.class);
        loader.setLocation(location);
        loader.setClassLoader(classLoader);
        rootPane = loader.load();
        controller = loader.getController();
        viewImagePath = null;
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
    public static class Builder<C> {
        private String id;
        private String title;
        private Position pos;
        private URL file;
        private String toolTipInfo;
        private double viewAreaSize;
        private ClassLoader classLoader;
        private URL viewImage;
        private Parent rootPane;
        private C controller;

        /**
         * Set the builder value "id"
         *
         * @return fluent builder interface
         */
        public Builder<C> withId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Set the builder value "title"
         *
         * @return fluent builder interface
         */
        public Builder<C> withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the builder value "pos"
         *
         * @return fluent builder interface
         */
        public Builder<C> withPos(Position pos) {
            this.pos = pos;
            return this;
        }

        /**
         * Set the builder value "file"
         *
         * @return fluent builder interface
         */
        public Builder<C> withFile(URL file) {
            this.file = file;
            return this;
        }

        /**
         * Set the builder value "file"
         *
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
         * @return fluent builder interface
         */
        public Builder<C> withToolTipInfo(String toolTipInfo) {
            this.toolTipInfo = toolTipInfo;
            return this;
        }

        /**
         * Set the builder value "viewAreaSize"
         *
         * @return fluent builder interface
         */
        public Builder<C> withViewAreaSize(double viewAreaSize) {
            this.viewAreaSize = viewAreaSize;
            return this;
        }

        /**
         * Set the builder value "classLoader"
         *
         * @return fluent builder interface
         */
        public Builder<C> withClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        /**
         * Set the builder value "viewImagePath"
         *
         * @return fluent builder interface
         */
        public Builder<C> withViewImage(URL viewImage) {
            this.viewImage = viewImage;
            return this;
        }

        /**
         * Set the builder value "viewImagePath"
         *
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
        public FXMLView<C> clone() {
            Objects.requireNonNull(id, "A view must have an unique id");
            Objects.requireNonNull(title, "A view must have a title");
            Objects.requireNonNull(pos, "The initial position must be set");
            // Objects.requireNonNull(controller, "Can not initialize a FXMLView without a controller.");

            return new FXMLView<>(id, title, pos, rootPane, toolTipInfo, viewAreaSize, controller, viewImage);
        }
    }
}
