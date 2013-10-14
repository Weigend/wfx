#set($symbol_pound='#')
#set($symbol_dollar='$')
#set($symbol_escape='\' )
package ${package}.application;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import javafx.application.*;
import java.io.IOException;

public class ApplicationActivator implements BundleActivator {

    private Lookup lookup = new Lookup(ApplicationActivator.class);

    public void start(final BundleContext context) throws Exception {

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    PreloaderNotificationService notificationService = lookup.lookup(PreloaderNotificationService.class);
                    notificationService.sendNotification(context.getBundle(), "Loading Example Application", 0);

                    FXMLView view = new FXMLView("${package}.editors:1", "Example Editor", Position.CENTER, "${packageInPathFormat}/editors/editor.fxml", getClass().getClassLoader());
                    FXMLView view1 = new FXMLView("${package}.editors:2", "Example Editor", Position.CENTER,
                            "${packageInPathFormat}/editors/editor.fxml", getClass().getClassLoader());
                    FXMLView view2 = new FXMLView("${package}.explorer:1", "Example explorer", Position.LEFT,
                            "${packageInPathFormat}/explorer/explorer.fxml", 0.25, getClass().getClassLoader());

                    WindowManager windowManager = lookup.lookup(WindowManager.class);
                    windowManager.register(view);
                    windowManager.register(view1);
                    windowManager.register(view2);

                    notificationService.sendNotification(context.getBundle(), "Finished loading Example Application", 1);
                    notificationService.sendNotification(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }
}
