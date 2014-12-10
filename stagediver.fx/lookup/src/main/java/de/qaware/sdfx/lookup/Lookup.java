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

import com.google.inject.Module;
import de.qaware.sdfx.lookup.impl.GuiceLookupStrategy;
import de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The general service lookup for the platform.
 * <p>
 * It can work with the osgi registry or with google guice if this module is not loaded with osgi.
 * <p>
 * This class is thread-safe.
 *
 * @author christian.fritz
 */
public final class Lookup {
    private static final Logger LOGGER = LoggerFactory.getLogger(Lookup.class);
    private static LookupStrategy lookupStrategy;

    /**
     * Initialize {@link de.qaware.sdfx.lookup.Lookup} with the given {@link de.qaware.sdfx.lookup.LookupStrategy}.
     *
     * @param lookupStrategy Use this strategy to lookup for instances.
     */
    public static void init(LookupStrategy lookupStrategy) {
        Lookup.lookupStrategy = lookupStrategy;
    }

    private Lookup() {
    }

    /**
     * Init the lookup for the given class.
     *
     * @param forClazz The class which want to use the lookup.
     * @deprecated Use instead {@link de.qaware.sdfx.lookup.Lookup#init(LookupStrategy)}
     */
    @Deprecated
    public Lookup(Class forClazz) {
        LOGGER.warn("Do not use 'new Lookup(Class forClazz)'. Use instead 'Lookup.init(LookupStrategy)'.");
    }

    /**
     * Init the lookup if the module is not running within an osgi container.
     *
     * @param modules A list of guice modules.
     * @deprecated Use instead {@link de.qaware.sdfx.lookup.Lookup#init(LookupStrategy)} with a {@link de.qaware.sdfx.lookup.impl.GuiceLookupStrategy}
     */
    @Deprecated
    public static void init(Module... modules) {
        lookupStrategy = new GuiceLookupStrategy(modules);
    }

    /**
     * Init the lookup if the module is not running within an osgi container.
     *
     * @param modules A list of guice modules.
     * @deprecated Use instead {@link de.qaware.sdfx.lookup.Lookup#init(LookupStrategy)} with a {@link de.qaware.sdfx.lookup.impl.GuiceLookupStrategy}
     */
    @Deprecated
    public static void init(Iterable<Module> modules) {
        lookupStrategy = new GuiceLookupStrategy(modules);
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
    public static <T> T lookup(Class<T> clazz) {
        return getLookupStrategy().lookup(clazz);
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
    public static <T> List<T> lookupAll(Class<T> clazz) {
        return getLookupStrategy().lookupAll(clazz);
    }

    /**
     * Get the current {@link de.qaware.sdfx.lookup.LookupStrategy}. If no lookup strategy found, it initialize the
     * {@link de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy} as default.
     *
     * @return The current lookup strategy or a new instance of the {@link de.qaware.sdfx.lookup.impl.ServiceLoaderLookupStrategy} if anyone exists.
     */
    public synchronized static LookupStrategy getLookupStrategy() {
        if (lookupStrategy == null) {
            lookupStrategy = new ServiceLoaderLookupStrategy();
        }
        return lookupStrategy;
    }
}
