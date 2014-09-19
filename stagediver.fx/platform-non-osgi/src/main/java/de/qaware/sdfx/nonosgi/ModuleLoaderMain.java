package de.qaware.sdfx.nonosgi;

import com.google.inject.Module;
import de.qaware.sdfx.lookup.Lookup;

import java.util.ServiceLoader;

/**
 * @author christian.fritz
 */
public class ModuleLoaderMain {

    public static void main(String[] args) {
        ServiceLoader<Module> loader = ServiceLoader.load(Module.class);

        Lookup.init(loader);
    }
}
