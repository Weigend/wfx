package de.qaware.sdfx.platform.impl;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.platform.api.EventBus;
import de.qaware.sdfx.platform.api.events.ProgressEvent;
import de.qaware.sdfx.platform.impl.eventbus.SimpleEventBus;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loadui.testfx.GuiTest;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.loadui.testfx.controls.Commons.hasText;
import static org.mockito.Mockito.when;

/**
 * Unit Test for the {@link de.qaware.sdfx.platform.impl.PreloaderController}.
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class PreloaderControllerTest extends GuiTest {

    private PreloaderController preloaderController;

    @Mock
    private LookupStrategy lookupStrategy;
    private EventBus eventBus = new SimpleEventBus();

    @Override
    protected Parent getRootNode() {
        try {
            when(lookupStrategy.lookup(EventBus.class)).thenReturn(eventBus);
            Lookup.init(lookupStrategy);
            URL resource = PreloaderControllerTest.class.getResource("/default/splash.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            Parent node = fxmlLoader.load();
            preloaderController = fxmlLoader.getController();
            return node;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testProgress() throws Exception {
        assertThat(getProgressText(), hasText("Loading..."));
        assertThat(getProgressBar().progressProperty().get(), is(equalTo(0.0)));
        Lookup.lookup(EventBus.class).publish(new ProgressEvent("TestMessage", 0.5, this));
        waitUntil(getProgressText(), not(hasText("Loading...")));
        assertThat(getProgressText().textProperty().get(), is(equalTo(("TestMessage"))));
        assertThat(getProgressBar().progressProperty().get(), is(equalTo(0.5)));
    }

    private Label getProgressText() throws NoSuchFieldException, IllegalAccessException {
        Field progressText = PreloaderController.class.getDeclaredField("progressText");
        progressText.setAccessible(true);
        return (Label) progressText.get(preloaderController);
    }

    private ProgressBar getProgressBar() throws NoSuchFieldException, IllegalAccessException {
        Field progressText = PreloaderController.class.getDeclaredField("progressBar");
        progressText.setAccessible(true);
        return (ProgressBar) progressText.get(preloaderController);
    }
}