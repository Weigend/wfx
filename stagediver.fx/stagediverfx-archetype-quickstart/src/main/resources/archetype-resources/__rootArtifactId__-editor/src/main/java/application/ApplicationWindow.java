#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.application;

import de.qaware.sdfx.lookup.Lookup;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class ApplicationWindow implements de.qaware.sdfx.windowmtg.api.ApplicationWindow {

    @FXML
    private MenuBar menuBar;

    @FXML
    private ToolBar toolbar;

    @FXML
    private HBox statusBar;

    @FXML
    private BorderPane root;

    private WindowManager windowManager;

    private Stage stage;

    private String defaultTitle;

    public void init() throws IOException {
        URL location = getClass().getClassLoader().getResource("${packageInPathFormat}/application/applicationWindow.fxml");
        FXMLLoader loader = new FXMLLoader(location);
        loader.setClassLoader(getClass().getClassLoader());
        loader.setController(this);
        Parent parent = (Parent) loader.load();
        stage.setScene(new Scene(parent));
        root.setCenter(Lookup.lookup(WindowManager.class).getRootPane());
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

    @FXML
    protected void restoreDefaultLayout() {
        Lookup.lookup(WindowManager.class).restoreDefaultLayout();
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
