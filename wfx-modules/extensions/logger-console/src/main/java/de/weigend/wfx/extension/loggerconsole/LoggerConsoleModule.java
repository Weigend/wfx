/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 Weigend AM
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
package de.weigend.wfx.extension.loggerconsole;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.platform.api.Module;
import de.weigend.wfx.platform.api.exceptions.PlatformException;
import de.weigend.wfx.windowmtg.api.ApplicationWindow;
import de.weigend.wfx.windowmtg.api.FXMLView;
import de.weigend.wfx.windowmtg.api.Position;
import de.weigend.wfx.windowmtg.api.WindowManager;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;

import java.io.IOException;

import static de.weigend.wfx.extension.uiutils.MenuUtil.*;

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
        windows.getItems().add(createMenuItem("loggerConsole", "Logger Console", showView(consoleView)));
    }

    @Override
    public void stop() {
        final WindowManager manager = Lookup.lookup(WindowManager.class);
        if (manager.hasRegisteredView(consoleView)) {
            manager.closeView(consoleView);
        }
    }
}
