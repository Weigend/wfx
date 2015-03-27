package de.qaware.sdfx.examplegui;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.event.ActionEvent;

import java.io.IOException;

/**
 * Example controller.
 *
 * @author christian.fritz
 */
public class ExampleController {

    private static int id = 0;

    /**
     * Add a new tab to the bottom of explorer view.
     *
     * @param actionEvent Ignored JavaFX event.
     * @throws IOException In case of the view can not be loaded.
     */
    public void addTabToExplorer(ActionEvent actionEvent) throws IOException {
        WindowManager windowManager = Lookup.lookup(WindowManager.class);
        View explorerView = windowManager.findView("example-explorer-1");

        View testView = new FXMLView("test-" + getNextViewId(), "Test View", Position.BOTTOM, "de/qaware/sdfx/examplegui/test.fxml");
        windowManager.register(testView, explorerView);
    }

    /**
     * Get the next id for views.
     *
     * @return the next view id.
     */
    private static int getNextViewId() {
        return id++;
    }
}
