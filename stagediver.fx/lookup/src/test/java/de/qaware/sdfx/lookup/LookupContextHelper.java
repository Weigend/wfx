package de.qaware.sdfx.lookup;

import com.google.inject.Injector;
import org.osgi.framework.BundleContext;

import java.lang.reflect.Field;

public class LookupContextHelper {

    public static void setWithinOsgi(boolean withinOsgi) throws IllegalAccessException, NoSuchFieldException {
        Field withinOsgiField = Lookup.class.getDeclaredField("withinOsgi");
        withinOsgiField.setAccessible(true);
        withinOsgiField.setBoolean(null, withinOsgi);
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
