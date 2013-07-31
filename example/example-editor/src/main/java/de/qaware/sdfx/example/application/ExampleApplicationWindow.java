package de.qaware.sdfx.example.application;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.io.IOException;

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
    private BorderPane root;
    private WindowManager windowManager;
    private Stage stage;
    private String defaultTitle;

    public void init() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(
                "/de/qaware/sdfx/example/application/appWindow.fxml"
        ));
        loader.setClassLoader(getClass().getClassLoader());
        loader.setController(this);
        Parent parent = (Parent) loader.load();
        stage.setScene(new Scene(parent));
        root.setCenter(lookup.lookup(WindowManager.class).getRootPane());
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
        lookup.lookup(WindowManager.class).restoreDefaultLayout();
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
