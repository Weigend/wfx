package de.qaware.sdfx.lookup.cdi;

import de.qaware.sdfx.lookup.LookupStrategy;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Using Contexts and Dependency Injection (CDI) as lookup strategy.
 *
 * @author christian.fritz
 */
@Singleton
public class CDILookupStrategy implements LookupStrategy {
    @Inject
    private Instance<Object> weldInstance;

    @Override
    public <T> T lookup(Class<T> clazz) {
        return weldInstance.select(clazz).get();
    }

    @Override
    public <T> List<T> lookupAll(Class<T> clazz) {
        Instance<T> select = weldInstance.select(clazz);
        List<T> instances = new ArrayList<>();
        for (T instance : select) {
            instances.add(instance);
        }
        return instances;
    }
}
