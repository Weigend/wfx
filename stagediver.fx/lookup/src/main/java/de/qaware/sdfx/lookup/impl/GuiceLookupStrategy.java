package de.qaware.sdfx.lookup.impl;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import de.qaware.sdfx.lookup.LookupStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses google guice to lookup instances.
 *
 * @author christian.fritz
 */
public class GuiceLookupStrategy implements LookupStrategy {

    private Injector injector;

    /**
     * Init the lookup if the module is not running within an osgi container.
     *
     * @param modules A list of guice modules.
     */
    public GuiceLookupStrategy(Module... modules) {
        injector = Guice.createInjector(modules);
    }

    /**
     * Init the lookup if the module is not running within an osgi container.
     *
     * @param modules A list of guice modules.
     */
    public GuiceLookupStrategy(Iterable<Module> modules) {
        injector = Guice.createInjector(modules);
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    @Override
    public <T> List<T> lookupAll(Class<T> clazz) {
        List<T> services = new ArrayList<>();
        services.add(injector.getInstance(clazz));
        return services;
    }
}
