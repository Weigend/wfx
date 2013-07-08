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

import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import de.qaware.sdfx.windowmtg.windows.DefaultApplicationWindow;

import javafx.application.*;
import javafx.scene.control.*;
import javafx.stage.*;

/**
 * Example main class to demonstrate running without osgi.
 */
public class NonOsgiMain extends Application {

    /**
     * This is the main method for demonstrating that the platform can be started without unsing osgi.
     *
     * @param args Commandline args
     */
    public static void main(String[] args) {
        Application.launch(NonOsgiMain.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("stagediver.fx Example Application");
        ApplicationWindow mainWindow = new DefaultApplicationWindow();
        mainWindow.setStage(stage);
        mainWindow.init();

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
