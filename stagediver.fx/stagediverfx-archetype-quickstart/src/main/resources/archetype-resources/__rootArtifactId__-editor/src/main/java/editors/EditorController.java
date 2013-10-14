#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.editors;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import javafx.fxml.*;
import javafx.scene.chart.*;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class EditorController implements Initializable {

    private static Lookup lookup = new Lookup(EditorController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // insert your init code here
    }
}
