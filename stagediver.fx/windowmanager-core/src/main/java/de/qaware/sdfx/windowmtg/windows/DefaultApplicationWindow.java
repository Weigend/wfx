//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-core
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 29.06.13 21:51
//      description: The main window of the stagediver.fx platform.
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.windows;

import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;

import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.io.IOException;

/**
 * This is the main window of the stagediver.fx platform. It supports the window management
 * and the default bars like menu, tool and status bar.
 */
@Component(name = "defaultAppWindow", immediate = true)
@Service(ApplicationWindow.class)
@Properties({
        @Property(name = "service.ranking", intValue = Integer.MIN_VALUE)
})
public class DefaultApplicationWindow implements ApplicationWindow {

    @FXML
    private MenuBar menuBar;
    @FXML
    private ToolBar toolbar;
    @FXML
    private HBox statusBar;
    private WindowManager windowManager;
    private Stage stage;
    private String defaultTitle;

    /**
     * Initialize the application window.
     *
     * @throws IOException In case of the requested fxml file can not be found or read.
     */
    public void init() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        FXMLLoader loader = new FXMLLoader(classLoader.getResource(
                "/de/qaware/sdfx/windowmtg/windows/DefaultApplicationWindow.fxml"
        ));
        loader.setClassLoader(classLoader);
        loader.setController(this);
        BorderPane rootPane = (BorderPane) loader.load();
        stage.setScene(new Scene(rootPane));
        rootPane.setCenter(windowManager.getRootPane());
    }

    /**
     * Get a list with all menu items.
     *
     * @return A list with the menu items.
     */
    @Override
    public ObservableList<Menu> getMenu() {
        return menuBar.getMenus();
    }

    /**
     * Get a list with all tool bar items.
     *
     * @return A list with the toolbar items.
     */
    @Override
    public ObservableList<Node> getToolbarItems() {
        return toolbar.getItems();
    }

    @Override
    public ObservableList<Node> getStatusBarItems() {
        return statusBar.getChildren();
    }

    @Override
    public WindowManager getWindowManager() {
        return windowManager;
    }

    @Override
    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    @Override
    public void restoreTitle() {
        stage.setTitle(defaultTitle);
    }

    @Override
    public String getTitle() {
        return stage.getTitle();
    }

    @Override
    public void setTitle(String title) {
        stage.setTitle(title);
    }

    @Override
    public Stage getStage() {
        return stage;
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        this.defaultTitle = stage.getTitle();
    }
}
