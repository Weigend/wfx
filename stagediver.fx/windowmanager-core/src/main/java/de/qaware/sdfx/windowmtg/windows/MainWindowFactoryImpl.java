//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: windowmanager-core
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 30.06.13 12:31
//      description:
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.windows;

import de.qaware.sdfx.windowmtg.api.MainWindow;
import de.qaware.sdfx.windowmtg.api.MainWindowFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.ops4j.peaberry.internal.FrameworkUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javafx.stage.*;

/**
 * Created with IntelliJ IDEA.
 * User: christian
 * Date: 30.06.13
 * Time: 12:31
 * To change this template use File | Settings | File Templates.
 */
public class MainWindowFactoryImpl implements MainWindowFactory {

    private static BundleContext context = FrameworkUtil.getBundle(MainWindowFactoryImpl.class).getBundleContext();

    @Override
    public MainWindow initMainWindow(Stage mainStage) {
        ServiceReference<MainWindow> reference = context.getServiceReference(MainWindow.class);
        MainWindow window;
        if (reference != null) {
            window = new MainWindowImpl(mainStage);
            context.registerService(MainWindow.class, window, null);
        }
        else {
            window = context.getService(reference);
        }
        return window;
    }
}
