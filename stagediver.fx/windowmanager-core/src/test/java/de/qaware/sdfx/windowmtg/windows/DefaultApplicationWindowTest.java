package de.qaware.sdfx.windowmtg.windows;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.WindowEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.loadui.testfx.GuiTest;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static de.qaware.sdfx.windowmtg.api.GuiTestHelper.getStage;
import static de.qaware.sdfx.windowmtg.api.GuiTestHelper.runInJavaFxThreadAndWait;
import static org.mockito.Mockito.*;

/**
 * Unit test for the {@link DefaultApplicationWindow}.
 *
 * @author christian.fritz
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultApplicationWindowTest extends GuiTest {
    private static LookupStrategy strategy = mock(LookupStrategy.class);
    @Mock
    private WindowEvent event;

    private DefaultApplicationWindow window = new DefaultApplicationWindow();

    static {
        Lookup.init(strategy);
        when(strategy.lookup(FXMLLoader.class)).thenAnswer(i -> new FXMLLoader());
        stage = getStage();
    }

    @Test
    public void testPlatformShutdownRequestHandler() throws Exception {
        window.setStage(stage);
        Platform.runLater(() -> window.platformShutdownRequestHandler(event));
        Thread.sleep(500);
        DialogPane dialogPane = find("#ShutdownDialog");
        Button button = (Button) dialogPane.lookupButton(ButtonType.NO);
        runInJavaFxThreadAndWait(button::fire);
        verify(event).consume();
    }

    @Override
    protected Parent getRootNode() {
        return new Label("");
    }
}