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
import org.osgi.framework.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The general service lookup for the platform.
 * <p/>
 * It can work with the osgi registry or with google guice if this module is not loaded with osgi.
 */
public final class Lookup {

    private static final String SERVICE_RANKING_PROP = "service.ranking";
    private static boolean withinOsgi;
    private static Injector injector;

    static {
        withinOsgi = Lookup.class.getClassLoader() instanceof BundleReference;
    }

    private BundleContext context;

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
     * Init the lookup if the module is not running within an osgi container.
     *
     * @param modules A list of guice modules.
     */
    public static void init(Iterable<Module> modules) {
        injector = Guice.createInjector(modules);
    }

    /**
     * Lookup a class from the registry.
     * <p/>
     * The returned service is that service that have the highest service ranking.
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

    /**
     * Lookup all services for one class from the registry.
     * <p/>
     * The list of services is ordered by the service ranking. The service with the highest ranking is the first.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A list with all found service instances for the searched class.
     */
    public <T> List<T> lookupAll(Class<T> clazz) {
        if (withinOsgi) {
            return lookupAllOsgi(clazz);
        }
        else {
            List<T> services = new ArrayList<>();
            services.add(injector.getInstance(clazz));
            return services;
        }
    }

    private <T> List<T> lookupAllOsgi(Class<T> clazz) {
        try {
            List<T> services = new ArrayList<>();
            List<ServiceReference<T>> references = new ArrayList<>(context.getServiceReferences(clazz, null));

            Collections.sort(references, new Comparator<ServiceReference<T>>() {
                @Override
                public int compare(ServiceReference<T> o1, ServiceReference<T> o2) {
                    Integer r1 = (Integer) (o1.getProperty(SERVICE_RANKING_PROP) == null ? 0 : o1.getProperty(SERVICE_RANKING_PROP));
                    Integer r2 = (Integer) (o2.getProperty(SERVICE_RANKING_PROP) == null ? 0 : o2.getProperty(SERVICE_RANKING_PROP));
                    return r2.compareTo(r1);
                }
            });

            for (ServiceReference<T> reference : references) {
                services.add(context.getService(reference));
            }
            return services;
        }
        catch (InvalidSyntaxException e) {
            return null;
        }
    }
}
