/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
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
package io.softwareecg.wfx.platform.core;

import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.platform.api.EventBus;
import io.softwareecg.wfx.platform.api.EventBusListener;
import io.softwareecg.wfx.platform.api.events.ProgressEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller for handling the splash screen.
 *
 */
public class ProgressController implements Initializable {

    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressText;

    private EventBusListener<ProgressEvent> progressListener;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        EventBus<ProgressEvent> eventBus = Lookup.lookup(EventBus.class);
        progressListener = this::progress;
        eventBus.subscribe(ProgressEvent.class, progressListener);

        progressBar.visibleProperty().bind(
                progressBar.progressProperty().isNotEqualTo(0).and(
                        progressBar.progressProperty().isEqualTo(1).not()
                )
        );
    }

    /**
     * Unsubscribe this controller from {@link ProgressEvent} on the EventBus.
     * Used by the splash screen to prevent module-internal progress events
     * from overriding the startup progress.
     */
    @SuppressWarnings("unchecked")
    public void unsubscribeProgressEvent() {
        if (progressListener != null) {
            EventBus<ProgressEvent> eventBus = Lookup.lookup(EventBus.class);
            eventBus.unsubscribe(ProgressEvent.class, progressListener);
            progressListener = null;
        }
    }

    /**
     * Registered by the Annotation to listen for a ProgressEvent.
     * Called by the IEventBus, when an event of that type has occured.
     *
     * @param event the ProgressEvent
     * @return true, if the event is still valid; false, event has been consumed
     */
    public boolean progress(final ProgressEvent event) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> progress(event));
            return true;
        }
        progressBar.progressProperty().setValue(event.getProgress());
        progressText.textProperty().setValue(event.getMessage());
        return true;
    }
}
