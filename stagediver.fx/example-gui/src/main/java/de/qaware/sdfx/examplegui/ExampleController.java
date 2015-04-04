/*
 * #%L
 * Example GUI Implementation for stagediver.fx
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.qaware.sdfx.examplegui;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import de.qaware.sdfx.windowmtg.api.WindowManager;

import javafx.event.*;
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
