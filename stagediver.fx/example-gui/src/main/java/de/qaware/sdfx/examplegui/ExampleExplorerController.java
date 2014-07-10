package de.qaware.sdfx.examplegui;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.View;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * This is a example editor controller.
 */
public class ExampleExplorerController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleExplorerController.class);

    private Lookup lookup = new Lookup(ExampleExplorerController.class);

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

    /**
     * Event handler for a click on a tree item.
     *
     * @param event the mouse event.
     */
    @FXML
    public void treeClicked(MouseEvent event) {
        LOGGER.info("treeClicked: {}", event);
    }

    /**
     * Focus the editor
     *
     * @param actionEvent the event to focus the editor.
     */
    public void focusEditor(ActionEvent actionEvent) {
        LOGGER.info("focus editor");
        WindowManager windowManager = lookup.lookup(WindowManager.class);
        View editorView = windowManager.findView("example-1");
        windowManager.showView(editorView);
    }
}
