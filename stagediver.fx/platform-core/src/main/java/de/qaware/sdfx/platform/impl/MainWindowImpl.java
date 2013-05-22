package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.platform.api.MainWindow;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

/**
 *
 */
public class MainWindowImpl implements MainWindow {

    @FXML
    private VBox root;

    @Override
    public void setWindowManager(WindowManager windowManager) {
        Parent rootPane = windowManager.getRootPane();
        if (!root.getChildren().contains(rootPane)) {
            root.getChildren().add(rootPane);
            windowManager.initialize(null, null);
        }
    }

    public void closeView(ActionEvent actionEvent) {
    }

    public void resetToDefaults(ActionEvent actionEvent) {
    }
}
