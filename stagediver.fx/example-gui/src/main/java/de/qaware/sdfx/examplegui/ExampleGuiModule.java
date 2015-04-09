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
import de.qaware.sdfx.platform.api.Module;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Register the a example view within the window manager.
 *
 * @author christian.fritz
 */
public class ExampleGuiModule implements Module {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleGuiModule.class);

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
            ClassLoader classLoader = getClass().getClassLoader();
            LOGGER.info("Register example view");
            FXMLView<ExampleController> center =
                    new FXMLView<>("example-1", "Example GUI", Position.CENTER,
                            "de/qaware/sdfx/examplegui/example.fxml", 0.7,
                            classLoader);

            FXMLView<ExampleExplorerController> explorer =
                    new FXMLView<>("example-explorer-1", "Example Explorer", Position.LEFT,
                            "de/qaware/sdfx/examplegui/example_explorer.fxml", 0.3,
                            classLoader);

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

    }


    /**
     * Stop the module.
     * <p>
     * This method will be called while platform shutdown.
     */
    @Override
    public void stop() {

    }
}
