package de.qaware.sdfx.platform.api;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import java.util.List;

import static org.ops4j.peaberry.Peaberry.osgiModule;

/**
 * Registry guice version.
 */
public class Registry {
    /**
     * Guice injector.
     */
    private static Injector injector;

    /**
     * No instantiation, static methods only.
     */
    private Registry() { /* emtpy */ }

    public static void init(Module... modules) {

        BundleContext context = FrameworkUtil.getBundle(Registry.class).getBundleContext();
        Module osgiModule = osgiModule(context);
        List<Module> moduleList = Lists.asList(osgiModule, modules);
        injector = Guice.createInjector(moduleList);
    }

    /**
     * Get a object for a given interface.
     *
     * @param clazz the class or interface
     * @return a bean.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getObject(final Class<T> clazz) {
        return injector.getInstance(clazz);
    }

}