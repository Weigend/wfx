#set($symbol_pound='#')
#set($symbol_dollar='$')
#set($symbol_escape='\' )
package ${package}.application;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.nonosgi.PlatformModule;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;

import javafx.application.*;
import javafx.stage.*;

public class ApplicationMain extends Application {

    private static Lookup lookup = new Lookup(ApplicationMain.class);

    public static void main(String[] args) {
        Lookup.init(new PlatformModule());
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ApplicationWindow window = new ApplicationWindow();
        window.setStage(stage);
        window.init();

        FXMLView view = new FXMLView("de.qaware.qstest.editors:1", "Example Editor", Position.CENTER,
                "${packageInPathFormat}/editors/editor.fxml", getClass().getClassLoader());
        FXMLView view1 = new FXMLView("de.qaware.qstest.editors:2", "Example Editor", Position.CENTER,
                "${packageInPathFormat}/editors/editor.fxml", getClass().getClassLoader());
        FXMLView view2 = new FXMLView("de.qaware.qstest.explorer:1", "Example explorer", Position.LEFT,
                "${packageInPathFormat}/explorer/explorer.fxml", 0.25, getClass().getClassLoader());

        WindowManager windowManager = lookup.lookup(WindowManager.class);
        windowManager.register(view);
        windowManager.register(view1);
        windowManager.register(view2);

        stage.show();
    }
}
