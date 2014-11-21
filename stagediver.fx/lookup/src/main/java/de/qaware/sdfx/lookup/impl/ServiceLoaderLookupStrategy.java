package de.qaware.sdfx.lookup.impl;

import de.qaware.sdfx.lookup.LookupStrategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Use the javas {@link java.util.ServiceLoader} to lookup the actual instances.
 *
 * @author christian.fritz
 */
public class ServiceLoaderLookupStrategy implements LookupStrategy {
    @Override
    public <T> T lookup(Class<T> clazz) {
        ServiceLoader<T> load = ServiceLoader.load(clazz);
        Iterator<T> iterator = load.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    @Override
    public <T> List<T> lookupAll(Class<T> clazz) {
        List<T> instances = new ArrayList<>();
        ServiceLoader<T> load = ServiceLoader.load(clazz);
        for (T aLoad : load) {
            instances.add(aLoad);
        }
        return instances;
    }
}
