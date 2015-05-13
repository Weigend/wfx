/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
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
package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.platform.api.EventSubscriber;
import de.qaware.sdfx.platform.api.events.ProgressEvent;
import de.qaware.sdfx.platform.api.events.StartupProgressEvent;
import de.qaware.sdfx.platform.impl.eventbus.AnnotationProcessor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for handling the splash screen.
 *
 * @author christian.fritz
 */
public class PreloaderController implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreloaderController.class);

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AnnotationProcessor.process(this);
        progressBar.visibleProperty().bind(
                progressBar.progressProperty().isNotEqualTo(0).and(
                        progressBar.progressProperty().isEqualTo(1).not()
                )
        );
    }

    /**
     * Registered by the Annotation to listen for a ProgressEvent.
     * Called by the IEventBus, when an event of that type has occured.
     *
     * @param event the ProgressEvent
     * @return true, if the event is still valid; false, event has been consumed
     */
    @EventSubscriber(eventClass = {ProgressEvent.class, StartupProgressEvent.class})
    public boolean progress(final ProgressEvent event) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> progress(event));
            return true;
        }
        if (event.getClass() == ProgressEvent.class) {
            LOGGER.warn("Use of \"de.qaware.sdfx.platform.api.events.ProgressEvent\" to report startup process is" +
                    " deprecated.\n\tPlease use \"de.qaware.sdfx.platform.api.events.StartupProgressEvent\" instead.");
        }
        progressBar.progressProperty().setValue(event.getProgress());
        progressText.textProperty().setValue(event.getMessage());
        return true;
    }
}
