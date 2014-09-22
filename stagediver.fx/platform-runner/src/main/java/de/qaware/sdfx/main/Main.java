package de.qaware.sdfx.main;

import com.google.inject.Module;
import de.qaware.sdfx.lookup.Lookup;

import java.util.ServiceLoader;

/**
 * @author christian.fritz
 */
public class Main {
    private static Lookup lookup = new Lookup(Main.class);

    public static void main(String[] args) {
        ServiceLoader<Module> loader = ServiceLoader.load(Module.class);

        Lookup.init(loader);

        lookup.lookup(PlatformRunner.class).run();
    }
}
