package de.qaware.sdfx.example.application;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.platform.api.PreloaderNotificationService;
import de.qaware.sdfx.windowmtg.api.FXMLView;
import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.WindowManager;
import javafx.application.Preloader;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 14.07.13
 * Time: 17:06
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationActivator implements BundleActivator {

    private Lookup lookup = new Lookup(ApplicationActivator.class);

    private PreloaderNotificationService notificationService = lookup.lookup(PreloaderNotificationService.class);

    public void start(BundleContext context) throws Exception {
        notificationService.sendNotification(context.getBundle(), "Loading Example Application", 0);

        FXMLView view = new FXMLView("sdfx.example.editor:1", "Example Editor", Position.CENTER, "de/qaware/sdfx/example/editor/example.fxml", getClass().getClassLoader());
        FXMLView view1 = new FXMLView("sdfx.example.explorer:1", "Example explorer", Position.LEFT, "de/qaware/sdfx/example/explorer/example_explorer.fxml", getClass().getClassLoader());

        WindowManager windowManager = lookup.lookup(WindowManager.class);
        windowManager.register(view);
        windowManager.register(view1);

        notificationService.sendNotification(context.getBundle(), "Finished loading Example Application", 1);
        notificationService.sendNotification(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
