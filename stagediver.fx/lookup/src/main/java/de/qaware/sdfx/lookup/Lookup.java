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

    protected static Injector getInjector() {
        return injector;
    }

    protected static void setInjector(Injector injector) {
        Lookup.injector = injector;
    }

    protected static boolean isWithinOsgi() {
        return withinOsgi;
    }

    protected static void setWithinOsgi(boolean withinOsgi) {
        Lookup.withinOsgi = withinOsgi;
    }

    /**
     * Init the lookup if the module is not running within an osgi container.
     *
     * @param modules A list of guice modules.
     */
    public static void init(Module... modules) {
        injector = Guice.createInjector(modules);
    }

    protected BundleContext getContext() {
        return context;
    }

    protected void setContext(BundleContext context) {
        this.context = context;
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
        List<T> services = new ArrayList<>();
        try {
            if (withinOsgi) {
                List<ServiceReference<T>> references = new ArrayList<>(context.getServiceReferences(clazz, null));

                Collections.sort(references, new Comparator<ServiceReference<T>>() {
                    @Override
                    public int compare(ServiceReference<T> o1, ServiceReference<T> o2) {
                        Integer r1 = (Integer) (o1.getProperty("service.ranking") != null ? o1.getProperty("service.ranking") : 0);
                        Integer r2 = (Integer) (o2.getProperty("service.ranking") != null ? o2.getProperty("service.ranking") : 0);
                        return r2.compareTo(r1);
                    }
                });

                for (ServiceReference<T> reference : references) {
                    services.add(context.getService(reference));
                }
            }
            else {
                //TODO: Guice Implementation
                services.add(injector.getInstance(clazz));
            }
        } catch (InvalidSyntaxException e) {
            return null;
        }
        return services;
    }
}
