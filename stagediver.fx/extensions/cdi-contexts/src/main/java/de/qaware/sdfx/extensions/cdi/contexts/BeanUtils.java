package de.qaware.sdfx.extensions.cdi.contexts;

import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;

/**
 * Utilities to work with cdi beans.
 *
 * @author christian.fritz
 */
public final class BeanUtils {

    private BeanUtils() {
    }

    /**
     * Check if the given bean is proxied.
     *
     * @param object the bean to check
     * @return true if the bean is proxied by cdi, otherwise false
     */
    public static boolean isProxied(Object object) {
        return object instanceof TargetInstanceProxy;
    }

    /**
     * Get the unwrapped bean for the current contexts.
     *
     * @param object the bean to unwrap.
     * @param <T>    The type of the bean to unwrap.
     * @return the unwrapped bean if it is proxied, otherwise {@code object}
     */
    @SuppressWarnings("unchecked")
    public static <T> T getUnwrappedInstance(T object) {
        if (isProxied(object)) {
            return ((TargetInstanceProxy<T>) object).getTargetInstance();
        }
        return object;
    }
}
