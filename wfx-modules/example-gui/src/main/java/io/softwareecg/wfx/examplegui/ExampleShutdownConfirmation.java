/*
 * #%L
 * Example GUI Implementation for wfx
 * %%
 * Copyright (C) 2013 - 2026 Weigend AM
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

import io.softwareecg.wfx.windowmanager.api.ShutdownConfirmation;
import jakarta.inject.Singleton;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

/**
 * Demonstrates the {@link ShutdownConfirmation} override pattern.
 * <p>
 * Avaje selects this {@code @Singleton} over the WFX-supplied
 * {@code DefaultShutdownConfirmation} because the WFX default carries
 * {@code @Secondary} — no extra precedence annotation needed here. The
 * dialog text is the only thing that differs from the default for now;
 * a real application would route through "save unsaved changes" logic
 * or show a branded FXML.
 */
@Singleton
public class ExampleShutdownConfirmation implements ShutdownConfirmation {

    @Override
    public boolean confirm(Stage owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Quit the WFX example application?",
                ButtonType.YES, ButtonType.NO);
        alert.initOwner(owner);
        alert.setTitle("Example GUI");
        alert.setHeaderText("Quit Example GUI");
        return alert.showAndWait()
                .filter(button -> button == ButtonType.YES)
                .isPresent();
    }
}
