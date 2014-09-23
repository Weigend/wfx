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

import java.util.ArrayList;
import java.util.List;

/**
 * The general service lookup for the platform.
 * <p>
 * It can work with the osgi registry or with google guice if this module is not loaded with osgi.
 */
public final class Lookup {

    private static final String SERVICE_RANKING_PROP = "service.ranking";
    private static boolean withinOsgi;
    private static Injector injector;

    /**
     * Init the lookup for the given class.
     *
     * @param forClazz The class which want to use the lookup.
     */
    @Deprecated
    public Lookup(Class forClazz) {
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
     * <p>
     * The returned service is that service that have the highest service ranking.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A instance of the requested class or null if not found.
     */
    public <T> T lookup(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    /**
     * Lookup all services for one class from the registry.
     * <p>
     * The list of services is ordered by the service ranking. The service with the highest ranking is the first.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A list with all found service instances for the searched class.
     */
    public <T> List<T> lookupAll(Class<T> clazz) {
        List<T> services = new ArrayList<>();
        services.add(injector.getInstance(clazz));
        return services;
    }
}
