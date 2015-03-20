package de.qaware.sdfx.windowmtg.impl;

import javafx.fxml.FXMLLoader;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;

/**
 * Produces the FXMLLoader when using the cdi module.
 *
 * @author christian.fritz
 */
public class FXMLLoaderProducer {
    @Inject
    Instance<Object> instance;

    @Produces
    public FXMLLoader createLoader() {
        return new FXMLLoader(null, null, null, param -> instance.select(param).get(), StandardCharsets.UTF_8);
    }
}
