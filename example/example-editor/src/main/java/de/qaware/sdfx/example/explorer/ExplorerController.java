package de.qaware.sdfx.example.explorer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import java.net.URL;
import java.util.ResourceBundle;

public class ExplorerController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExplorerController.class);

    @FXML
    protected TreeView<String> tree;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TreeItem<String> root = new TreeItem<>("Root Node");
        root.setExpanded(true);
        root.getChildren().addAll(
                new TreeItem<>("Item 1"),
                new TreeItem<>("Item 2"),
                new TreeItem<>("Item 3")
        );
        tree.setRoot(root);
    }

    @FXML
    public void treeClicked(MouseEvent event) {
        LOGGER.info("treeClicked: {}", event);
    }
}
