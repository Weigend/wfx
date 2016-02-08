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

import de.qaware.sdfx.extension.systemviews.SystemViewsHelper;
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
        SystemViewsHelper.addViewOverview();
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
