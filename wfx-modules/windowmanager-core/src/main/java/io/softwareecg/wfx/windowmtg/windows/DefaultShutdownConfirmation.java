/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
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
package io.softwareecg.wfx.windowmtg.windows;

import io.avaje.inject.Secondary;
import io.softwareecg.wfx.lookup.Lookup;
import io.softwareecg.wfx.windowmtg.api.ShutdownConfirmation;
import jakarta.inject.Singleton;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * Default {@link ShutdownConfirmation} that shows a Yes/No dialog loaded
 * from {@code ShutdownDialog.fxml}.
 * <p>
 * Marked {@link Secondary} so any application-provided
 * {@code @Singleton ShutdownConfirmation} bean wins via Avaje DI without
 * the application having to add {@code @Primary}. ServiceLoader-based
 * applications can replace the default by initialising
 * {@link io.softwareecg.wfx.lookup.impl.ServiceLoaderLookupStrategy} with
 * their own implementation before the platform launches.
 *
 * @since 1.1.0
 */
@Secondary
@Singleton
public class DefaultShutdownConfirmation implements ShutdownConfirmation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultShutdownConfirmation.class);
    private static final String DIALOG_FXML = "/io/softwareecg/wfx/windowmtg/windows/ShutdownDialog.fxml";

    @Override
    public boolean confirm(Stage owner) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setResultConverter(button -> Objects.equals(button, ButtonType.YES));

        FXMLLoader loader = Lookup.lookup(FXMLLoader.class);
        loader.setLocation(getClass().getResource(DIALOG_FXML));
        try {
            dialog.setDialogPane((DialogPane) loader.load());
        }
        catch (IOException e) {
            // Fail-open on FXML load failure — the user wanted to close,
            // do not strand them with an unresponsive button. The error
            // is logged so the broken dialog resource can be diagnosed.
            LOGGER.error("Unable to load shutdown dialog from {}, proceeding with shutdown.", DIALOG_FXML, e);
            return true;
        }
        return dialog.showAndWait().orElse(false);
    }
}
