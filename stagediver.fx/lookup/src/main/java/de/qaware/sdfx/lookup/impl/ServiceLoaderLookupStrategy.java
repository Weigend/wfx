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

    /**
     * Initializes the internal structure to be able to lookup services.
     *
     * @param o the 2d-Array with the classes mapped to services
     */
    public void init(Object[][] o) {
        for (Object[] objects : o) {
            Class clazz = (Class) objects[0];
            Object value = objects[1];
            lookupCache.put(clazz, value);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T lookup(Class<T> clazz) {
        List<T> objects = lookupAll(clazz);
        if (objects.size() > 0) {
            if (objects.get(0) instanceof Producer) {
                return ((Producer<T>) objects.get(0)).getInstance();
            }
            return objects.get(0);
        }
        else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
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

    public static interface Producer<T> {
        T getInstance();
    }
}
