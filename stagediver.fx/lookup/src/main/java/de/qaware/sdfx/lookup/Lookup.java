//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: lookup
//  ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 07.07.13 10:52
//      description: Lookup services.
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.lookup;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

/**
 * The general service lookup for the platform.
 * <p/>
 * It can work with the osgi registry or with google guice if this module is not loaded with osgi.
 */
public class Lookup {

    private BundleContext context;

    private static boolean withinOsgi;

    private static Injector injector;

    static {
        withinOsgi = Lookup.class.getClassLoader() instanceof BundleReference;
    }

    /**
     * Init the lookup for the given class.
     *
     * @param forClazz The class which want to use the lookup.
     */
    public Lookup(Class forClazz) {
        if (withinOsgi) {
            context = FrameworkUtil.getBundle(forClazz).getBundleContext();
        }
    }

    /**
     * Init the lookup if the module is not running within an osgi container.
     *
     * @param modules A list of guice modules.
     */
    public static void init(Module... modules) {
        injector = Guice.createInjector(modules);
    }

    /**
     * Lookup a class from the registry.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A instance of the requested class or null if not found.
     */
    public <T> T lookup(Class<T> clazz) {
        if (withinOsgi) {
            ServiceReference<T> reference = context.getServiceReference(clazz);
            if (reference != null) {
                return context.getService(reference);
            }
            return null;
        }
        else {
            return injector.getInstance(clazz);
        }
    }
}
