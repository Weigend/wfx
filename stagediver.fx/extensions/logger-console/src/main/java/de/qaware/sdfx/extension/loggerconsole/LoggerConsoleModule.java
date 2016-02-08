/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 QAware GmbH
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
package de.qaware.sdfx.extension.loggerconsole;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.Module;
import de.qaware.sdfx.platform.api.exceptions.PlatformException;
import de.qaware.sdfx.windowmtg.api.ApplicationWindow;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;

import java.io.IOException;

import static de.qaware.sdfx.extension.uiutils.MenuUtil.createMenuItem;
import static de.qaware.sdfx.extension.uiutils.MenuUtil.findOrCreateItem;

/**
 * Module to load the logger console.
 *
 * @author christian.fritz
 */
public class LoggerConsoleModule implements Module {

    private FXMLView<ConsoleController> consoleView;

    @Override
    public void preload() throws PlatformException {
        try {
            consoleView = new FXMLView.Builder<ConsoleController>()
                    .withFile(getClass().getResource("Console.fxml"))
                    .withTitle("Logger Console")
                    .withId("loggerConsole:1")
                    .withViewAreaSize(0.25)
                    .withPos(Position.BOTTOM)
                    .withViewImage(getClass().getResource("log.png"))
                    .build();
        }
        catch (IOException e) {
            throw new PlatformException("Unable to load Logger Console.", e);
        }
    }

    @Override
    public void start() {
        final ApplicationWindow appWindow = Lookup.lookup(ApplicationWindow.class);
        ObservableList<Menu> menu = appWindow.getMenu();
        Menu view = findOrCreateItem(menu, "view", () -> new Menu("View"), 2);
        Menu windows = (Menu) findOrCreateItem(view.getItems(), "windows", () -> new Menu("Windows"), 0);
        windows.getItems().add(createMenuItem("loggerConsole", "Logger Console", (e) -> showLoggerConsole()));
    }

    @Override
    public void stop() {
        final WindowManager manager = Lookup.lookup(WindowManager.class);
        if (manager.hasRegisteredView(consoleView)) {
            manager.closeView(consoleView);
        }
    }

    /**
     * Action handler to show the logging console.
     */
    private void showLoggerConsole() {
        final WindowManager manager = Lookup.lookup(WindowManager.class);
        if (!manager.hasRegisteredView(consoleView)) {
            manager.register(consoleView);
        }
        else {
            manager.showView(consoleView);
        }
    }
}
