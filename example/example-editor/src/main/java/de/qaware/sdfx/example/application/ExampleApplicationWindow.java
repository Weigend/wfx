package de.qaware.sdfx.example.application;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 07.07.13
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */
@Component(immediate = true)
@Service(ApplicationWindow.class)
public class ExampleApplicationWindow implements ApplicationWindow {
    private static Lookup lookup = new Lookup(ExampleApplicationWindow.class);
    @FXML
    private MenuBar menuBar;
    @FXML
    private ToolBar toolbar;
    @FXML
    private HBox statusBar;
    @FXML
    private Pane windowManagerArea;

    private WindowManager windowManager;
    private Stage stage;
    private String defaultTitle;

    public void init() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(
                    "/de/qaware/sdfx/example/editor/appWindow.fxml"
            ));
            loader.setClassLoader(getClass().getClassLoader());
            loader.setController(this);
            Parent parent = (Parent) loader.load();
            stage.setScene(new Scene(parent));
            windowManagerArea.getChildren().clear();
            windowManagerArea.getChildren().add(windowManager.getRootPane());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    public void setTitle(String title) {
        stage.setTitle(title);
    }

    @Override
    public String getTitle() {
        return stage.getTitle();
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
