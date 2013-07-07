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

import de.qaware.sdfx.lookup.Lookup;
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
    private static Lookup lookup = new Lookup(DefaultApplicationWindow.class);
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
                    "/de/qaware/sdfx/windowmtg/windows/DefaultApplicationWindow.fxml"
            ));
            loader.setClassLoader(getClass().getClassLoader());
            loader.setController(this);
            Parent parent = (Parent) loader.load();
            stage.setScene(new Scene(parent));
            windowManagerArea.getChildren().clear();
            windowManagerArea.getChildren().add(windowManager.getRootPane());
        }
        catch (IOException e) {
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
