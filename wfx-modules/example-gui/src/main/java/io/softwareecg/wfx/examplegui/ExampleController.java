/*
 * #%L
 * Example GUI Implementation for wfx
 * %%
 * Copyright (C) 2013 - 2015 Weigend AM
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
package io.softwareecg.wfx.examplegui;

import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.windowmanager.api.FXMLView;
import io.softwareecg.wfx.windowmanager.api.Position;
import io.softwareecg.wfx.windowmanager.api.View;
import io.softwareecg.wfx.windowmanager.api.ViewKind;
import io.softwareecg.wfx.windowmanager.api.WindowManager;
import javafx.event.ActionEvent;

import java.io.IOException;

/**
 * Example controller.
 *
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

        // Demonstrates the DOCUMENT kind: each click spawns a transient tab.
        // Closing the tab unregisters it, restoreDefaultLayout drops them all,
        // and they do NOT clutter the auto-built View menu.
        FXMLView<Object> testView = new FXMLView.Builder<>().withId("test-" + getNextViewId())
                .withTitle("Test View")
                .withPos(Position.BOTTOM)
                .withKind(ViewKind.DOCUMENT)
                .withFile(getClass().getResource("test.fxml"))
                .build();

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
