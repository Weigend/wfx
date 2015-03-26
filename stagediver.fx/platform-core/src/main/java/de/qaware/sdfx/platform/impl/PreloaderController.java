package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.platform.api.EventSubscriber;
import de.qaware.sdfx.platform.api.events.ProgressEvent;
import de.qaware.sdfx.platform.impl.eventbus.AnnotationProcessor;
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
 * @author christian.fritz
 */
public class PreloaderController implements Initializable {

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
    @EventSubscriber(eventClass = ProgressEvent.class)
    public boolean progress(final ProgressEvent event) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> progress(event));
        }
        progressBar.progressProperty().setValue(event.getProgress());
        progressText.textProperty().setValue(event.getMessage());
        return true;
    }
}
