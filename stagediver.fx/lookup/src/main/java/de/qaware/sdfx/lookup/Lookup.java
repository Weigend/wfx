package de.qaware.sdfx.lookup;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class Lookup {

    BundleContext context;

    public Lookup(Class forClazz) {
        context = FrameworkUtil.getBundle(forClazz).getBundleContext();
    }

    public <T> T lookup(Class<T> clazz) {
        return context.getService(context.getServiceReference(clazz));
    }
}
