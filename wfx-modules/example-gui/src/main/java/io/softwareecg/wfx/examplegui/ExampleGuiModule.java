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

import io.softwareecg.wfx.extension.systemviews.SystemViewsHelper;
import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.platform.api.Module;
import io.softwareecg.wfx.windowmanager.api.FXMLView;
import io.softwareecg.wfx.windowmanager.api.Position;
import io.softwareecg.wfx.windowmanager.api.WindowManager;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Register the a example view within the window manager.
 *
 */
@Singleton
public class ExampleGuiModule implements Module {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleGuiModule.class);

    /**
     * Where the example keeps the arrangement of its views between runs.
     */
    private static final Path LAYOUT_FILE =
            Path.of(System.getProperty("user.home"), ".wfx-example", "layout.properties");

    /**
     * Get the human readable module name.
     *
     * @return The module name.
     */
    @Override
    public String getName() {
        return "example gui module";
    }

    /**
     * Get the version of this module.
     *
     * @return The version of the module.
     */
    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    /**
     * Preload the module while starting the application.
     * <p>
     * It will be executed in an separate thread while showing the splash screen.
     */
    @Override
    public void preload() {
        final WindowManager manager = Lookup.lookup(WindowManager.class);
        try {
            LOGGER.info("Register example view");
            FXMLView<ExampleController> center = new FXMLView.Builder<ExampleController>()
                    .withId("example-1")
                    .withTitle("Example GUI")
                    .withPos(Position.CENTER)
                    .withFile(getClass().getResource("example.fxml"))
                    .withViewAreaSize(0.7)
                    .withViewImage(getClass().getResource("test-icon.png"))
                    .build();

            FXMLView<ExampleExplorerController> explorer = new FXMLView.Builder<ExampleExplorerController>()
                    .withId("example-explorer-1")
                    .withTitle("Example Explorer")
                    .withPos(Position.LEFT)
                    .withViewAreaSize(0.3)
                    .withFile(getClass().getResource("example_explorer.fxml"))
                    .build();

            manager.register(center);
            manager.register(explorer, center);
            Thread.sleep(5000);
        }
        catch (IOException | InterruptedException e) {
            LOGGER.error("Can not start module", e);
        }
    }

    @Override
    public void start() {
        SystemViewsHelper.addStandardWindowActions();
        restoreLayout();
    }

    /**
     * Stop the module.
     * <p>
     * This method will be called while platform shutdown.
     */
    @Override
    public void stop() {
        saveLayout();
    }

    /**
     * Arrange the views the way the user left them in the previous run. All
     * views are registered by now, so the saved layout finds them.
     */
    private void restoreLayout() {
        if (!Files.isReadable(LAYOUT_FILE)) {
            return;
        }
        try {
            Lookup.require(WindowManager.class).restoreLayout(Files.readString(LAYOUT_FILE));
        }
        catch (IOException e) {
            LOGGER.warn("Can not read the saved layout, starting with the default one", e);
        }
    }

    private void saveLayout() {
        try {
            Files.createDirectories(LAYOUT_FILE.getParent());
            Files.writeString(LAYOUT_FILE, Lookup.require(WindowManager.class).saveLayout());
        }
        catch (IOException e) {
            LOGGER.warn("Can not save the layout", e);
        }
    }
}
