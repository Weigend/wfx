// ______________________________________________________________________________
//         Project: stagediver.fx
// ______________________________________________________________________________
//
//      created by: christian.fritz
//   creation date: 23.05.13 10:25
//     description: Default implementation for fxml based views.
// ______________________________________________________________________________
//
//       Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.api;

import com.google.common.base.Preconditions;

import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import java.io.IOException;
import java.net.URL;

/**
 * This is the default implementation of a window management view. It loads the view from an fxml file and defines the
 * other needed values.
 *
 * @param <C> Defines the type of the controller class.
 */
public class FXMLView<C> implements View {

    public static final double DEFAULT_VIEW_AREA_SIZE = 0.5;
    private final String id;
    private final String title;
    private final Position defaultPosition;
    private final Pane rootPane;
    private final String toolTipInfo;
    private final double viewAreaSize;
    private final C controller;

    /**
     * Get a new view with the specified values.
     * <p/>
     * The resulted view will not have a tooltip.
     *
     * @param id          The view id.
     * @param title       The view title.
     * @param pos         The initial position of the view.
     * @param file        The path to the fxml file.
     * @param classLoader The class loader to resolve the fxml file and its controller.
     * @throws IOException In case of the view can not be loaded.
     */
    public FXMLView(String id, String title, Position pos, String file, ClassLoader classLoader) throws IOException {
        this(id, title, pos, file, null, DEFAULT_VIEW_AREA_SIZE, classLoader);
    }

    /**
     * Get a new view with the specified values.
     * <p/>
     * The resulted view will not have a tooltip.
     *
     * @param id           The view id.
     * @param title        The view title.
     * @param pos          The initial position of the view.
     * @param file         The path to the fxml file.
     * @param viewAreaSize The view area size. See {@link de.qaware.sdfx.windowmtg.api.View#getViewAreaSize()}.
     * @param classLoader  The class loader to resolve the fxml file and its controller.
     * @throws IOException In case of the view can not be loaded.
     */
    public FXMLView(String id, String title, Position pos, String file, double viewAreaSize, ClassLoader classLoader) throws IOException {
        this(id, title, pos, file, null, viewAreaSize, classLoader);
    }

    /**
     * Get a new view with the specified values.
     * <p/>
     * The view will show a tooltip info on mouse over.
     *
     * @param id          The view id.
     * @param title       The view title.
     * @param pos         The initial position of the view.
     * @param file        The path to the fxml file.
     * @param toolTipInfo The tooltip info.
     * @param classLoader The class loader to resolve the fxml file and its controller.
     * @throws IOException In case of the view can not be loaded.
     */
    public FXMLView(String id, String title, Position pos, String file, String toolTipInfo, ClassLoader classLoader)
            throws IOException {
        this(id, title, pos, file, toolTipInfo, DEFAULT_VIEW_AREA_SIZE, classLoader);
    }

    /**
     * Get a new view with the specified values.
     * <p/>
     * The view will show a tooltip info on mouse over.
     *
     * @param id           The view id.
     * @param title        The view title.
     * @param pos          The initial position of the view.
     * @param file         The path to the fxml file.
     * @param toolTipInfo  The tooltip info.
     * @param viewAreaSize The view area size. See {@link de.qaware.sdfx.windowmtg.api.View#getViewAreaSize()}.
     * @param classLoader  The class loader to resolve the fxml file and its controller.
     * @throws IOException In case of the view can not be loaded.
     */
    public FXMLView(String id, String title, Position pos, String file, String toolTipInfo, double viewAreaSize, ClassLoader classLoader)
            throws IOException {

        Preconditions.checkNotNull(classLoader);
        this.id = id;
        this.title = title;
        this.defaultPosition = pos;
        this.toolTipInfo = toolTipInfo;
        this.viewAreaSize = viewAreaSize;

        URL location = classLoader.getResource(file);
        FXMLLoader loader = new FXMLLoader(location);
        loader.setClassLoader(classLoader);
        rootPane = (Pane) loader.load();
        controller = loader.getController();
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
}
