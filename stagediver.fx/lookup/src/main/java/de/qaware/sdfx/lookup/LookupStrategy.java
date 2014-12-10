package de.qaware.sdfx.lookup;

import java.util.List;

/**
 * @author christian.fritz
 */
public interface LookupStrategy {
    /**
     * Lookup a class from the registry.
     * <p>
     * The returned service is that service that have the highest service ranking.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A instance of the requested class or null if not found.
     */
    public <T> T lookup(Class<T> clazz);

    /**
     * Lookup all services for one class from the registry.
     * <p>
     * The list of services is ordered by the service ranking. The service with the highest ranking is the first.
     *
     * @param clazz The class to search.
     * @param <T>   The type of the class to search.
     * @return A list with all found service instances for the searched class.
     */
    public <T> List<T> lookupAll(Class<T> clazz);
}
