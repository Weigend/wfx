/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
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
package de.weigend.wfx.platform.impl;

import de.weigend.wfx.lookup.Lookup;
import de.weigend.wfx.lookup.LookupStrategy;
import de.weigend.wfx.platform.api.EventBus;
import de.weigend.wfx.platform.api.events.ProgressEvent;
import de.weigend.wfx.platform.impl.eventbus.SimpleEventBus;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.loadui.testfx.controls.Commons.hasText;
import static org.mockito.Mockito.when;
/**
 * Unit Test for the {@link ProgressController}.
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class ProgressControllerTest  {

    private ProgressController preloaderController;

    @Mock
    private LookupStrategy lookupStrategy;

    private EventBus eventBus = new SimpleEventBus();

    private Parent node;

    @Before
    public void getRootNode() {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown); // startet das JavaFX Toolkit
            latch.await();
            when(lookupStrategy.lookup(EventBus.class)).thenReturn(eventBus);
            Lookup.init(lookupStrategy);
            URL resource = ProgressControllerTest.class.getResource("/default/splash.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            node = fxmlLoader.load();
            preloaderController = fxmlLoader.getController();
         }
        catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testProgress() throws Exception {
        assertThat(getProgressText(), hasText("Loading..."));
        assertThat(getProgressBar().progressProperty().get(), is(equalTo(0.0)));
        Lookup.lookup(EventBus.class).publish(new ProgressEvent("TestMessage", 0.5, this));
        waitUntilTextDiffers(getProgressText(), "Loading...");
        assertThat(getProgressText().textProperty().get(), is(equalTo(("TestMessage"))));
        assertThat(getProgressBar().progressProperty().get(), is(equalTo(0.5)));
    }

    private Label getProgressText() throws NoSuchFieldException, IllegalAccessException {
        Field progressText = ProgressController.class.getDeclaredField("progressText");
        progressText.setAccessible(true);
        return (Label) progressText.get(preloaderController);
    }

    private ProgressBar getProgressBar() throws NoSuchFieldException, IllegalAccessException {
        Field progressText = ProgressController.class.getDeclaredField("progressBar");
        progressText.setAccessible(true);
        return (ProgressBar) progressText.get(preloaderController);
    }

    private void waitUntilTextDiffers(Label label, String text) {

        for(int i = 0; i < 200; i++) {
            String labelText = label.getText();

            if (!text.equals(labelText)) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
