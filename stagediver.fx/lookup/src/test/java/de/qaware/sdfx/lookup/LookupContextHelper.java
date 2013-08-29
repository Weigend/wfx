package de.qaware.sdfx.lookup;

import com.google.inject.Injector;
import org.osgi.framework.BundleContext;

import java.lang.reflect.Field;

/**
 * This is a helper class to read and write some values from the lookup module through reflection
 * It was introduced to avoid changes caused by testing within the productive code.
 */
public class LookupContextHelper {

    public static void setWithinOsgi(boolean withinOsgi) throws IllegalAccessException, NoSuchFieldException {
        Field withinOsgiField = Lookup.class.getDeclaredField("withinOsgi");
        withinOsgiField.setAccessible(true);
        withinOsgiField.setBoolean(null, withinOsgi);
    }

    public static Injector getInjector() throws IllegalAccessException, NoSuchFieldException {
        Field injectorField = Lookup.class.getDeclaredField("injector");
        injectorField.setAccessible(true);
        return (Injector) injectorField.get(null);
    }

    public static void setInjector(Injector injector) throws IllegalAccessException, NoSuchFieldException {
        Field injectorField = Lookup.class.getDeclaredField("injector");
        injectorField.setAccessible(true);
        injectorField.set(null, injector);
    }

    public static void setContext(BundleContext context, Lookup lookupInstance) throws IllegalAccessException, NoSuchFieldException {
        Field contextField = Lookup.class.getDeclaredField("context");
        contextField.setAccessible(true);
        contextField.set(lookupInstance, context);
    }
}
