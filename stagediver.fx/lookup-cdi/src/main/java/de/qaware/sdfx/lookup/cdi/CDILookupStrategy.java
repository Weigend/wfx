package de.qaware.sdfx.lookup.cdi;

import de.qaware.sdfx.lookup.Lookup;
import de.qaware.sdfx.lookup.LookupStrategy;
import de.qaware.sdfx.lookup.Priority;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Using Contexts and Dependency Injection (CDI) as lookup strategy.
 *
 * @author christian.fritz
 */
@Singleton
public class CDILookupStrategy implements LookupStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDILookupStrategy.class);

    @Inject
    private Instance<Object> weldInstance;

    /**
     * Initialize the Lookup module.
     */
    public static void initLookup() {
        Weld weld = new Weld();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("Shutdown Weld");
                weld.shutdown();
            }
        });
        WeldContainer container = weld.initialize();
        Lookup.init(container.instance().select(CDILookupStrategy.class).get());
        LOGGER.info("Successfully initialized Weld/CDI and Lookup");
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        return weldInstance.select(clazz).get();
    }

    @Override
    public <T> List<T> lookupAll(Class<T> clazz) {
        Instance<T> select = weldInstance.select(clazz);
        return StreamSupport.stream(select.spliterator(), false)
                .map(t -> {
                    if (t.getClass().isAnnotationPresent(Priority.class)) {
                        return Pair.of(t, t.getClass().getAnnotation(Priority.class).value());
                    }
                    return Pair.of(t, 0);
                })
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .map(Pair::getKey)
                .collect(Collectors.toList());
    }
}
