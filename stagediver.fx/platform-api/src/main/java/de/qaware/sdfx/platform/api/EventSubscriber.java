package de.qaware.sdfx.platform.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Subscribes events.
 *
 * @author christian.fritz
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface EventSubscriber {

    /**
     * Method for getting the class.
     */
    Class eventClass();
}
