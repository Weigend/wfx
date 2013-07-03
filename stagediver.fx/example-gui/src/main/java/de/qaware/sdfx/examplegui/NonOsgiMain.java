//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: example-gui
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 03.07.13 13:27
//      description:
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.examplegui;

import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.MainWindow;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import de.qaware.sdfx.windowmtg.windows.MainWindowImpl;

import javafx.application.*;
import javafx.scene.control.*;
import javafx.stage.*;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 03.07.13
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public class NonOsgiMain extends Application {
    public static void main(String[] args) {
        Application.launch(NonOsgiMain.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("stagediver.fx Example Application");
        MainWindow mainWindow = new MainWindowImpl();
        mainWindow.setStage(stage);
        mainWindow.initialize(null, null);

        Menu file = new Menu("File");
        file.getItems().add(new MenuItem("New"));

        mainWindow.getMenu().add(file);

        WindowManager manager = mainWindow.getWindowManager();

        FXMLView<ExampleController> center =
                new FXMLView<>("example-1", "Example GUI", Position.CENTER,
                        "de/qaware/sdfx/examplegui/example.fxml", getClass().getClassLoader());

        FXMLView<ExampleExplorerController> explorer =
                new FXMLView<>("example-explorer-1", "Example Explorer", Position.LEFT,
                        "de/qaware/sdfx/examplegui/example_explorer.fxml", getClass().getClassLoader());


        manager.register(center);
        manager.register(explorer, center);

    }
}
