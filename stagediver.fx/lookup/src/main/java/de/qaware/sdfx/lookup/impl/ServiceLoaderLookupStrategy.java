package de.qaware.sdfx.lookup.impl;

import com.google.common.collect.ArrayListMultimap;
import de.qaware.sdfx.lookup.LookupStrategy;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Use the javas {@link java.util.ServiceLoader} to lookup the actual instances.
 *
 * @author christian.fritz
 */
public class ServiceLoaderLookupStrategy implements LookupStrategy {

    private ArrayListMultimap<Class, Object> lookupCache = ArrayListMultimap.create();

    @Override
    public <T> T lookup(Class<T> clazz) {
        List<T> objects = lookupAll(clazz);
        if (objects.size() > 0) {
            return objects.get(0);
        }
        else {
            return null;
        }
    }

    @Override
    public <T> List<T> lookupAll(Class<T> clazz) {
        if (lookupCache.containsKey(clazz)) {
            return (List<T>) lookupCache.get(clazz);
        }
        ServiceLoader<T> load = ServiceLoader.load(clazz);
        for (T instance : load) {
            lookupCache.put(clazz, instance);
        }
        return (List<T>) lookupCache.get(clazz);
    }
}
