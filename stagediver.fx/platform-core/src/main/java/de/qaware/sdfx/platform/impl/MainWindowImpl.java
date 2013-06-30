package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.platform.api.MainWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.*;

/**
 *
 */
@Deprecated
public class MainWindowImpl implements MainWindow {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainWindowImpl.class);

    @FXML
    private VBox root;

    private WindowManager windowManager;
    private Stage stage;
    private String defaultTitle;

    protected void setStage(Stage stage) {
        this.stage = stage;
        this.defaultTitle = stage.getTitle();
    }

    @Override
    public void setTitle(String title) {
        stage.setTitle(title + " | " + defaultTitle);
    }

    @Override
    public void restoreTitle() {
        stage.setTitle(defaultTitle);
    }

    @Override
    public void setWindowManager(WindowManager windowManager) {
        Parent rootPane = windowManager.getRootPane();
        if (!root.getChildren().contains(rootPane)) {
            root.getChildren().add(rootPane);
            windowManager.initialize(null, null);
        }
        this.windowManager = windowManager;
    }

    public void closeView(ActionEvent event) {

    }

    public void resetToDefaults(ActionEvent actionEvent) {
        if (windowManager != null) {
            LOGGER.info("Restore default layout. Event: {}", actionEvent);
            windowManager.restoreDefaultLayout();
        }
    }
}
