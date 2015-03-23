package de.qaware.sdfx.windowmtg.impl;

import javafx.fxml.FXMLLoader;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;

/**
 * Produces the FXMLLoader when using the cdi module.
 *
 * @author christian.fritz
 */
@Singleton
public class FXMLLoaderProducer {

    @Inject
    private Instance<Object> instance;

    /**
     * Producer method to create new instances of the {@link javafx.fxml.FXMLLoader} which initializes the controllers
     * with cdi.
     *
     * @return The cdi specific fxml loader.
     */
    @Produces
    public FXMLLoader createLoader() {
        return new FXMLLoader(null, null, null, controllerClass -> instance.select(controllerClass).get(), StandardCharsets.UTF_8);
    }
}
