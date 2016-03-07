package de.qaware.sdfx.extensions.cdi.contexts.api;

import javax.enterprise.context.NormalScope;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The scope annotation for the {@link ViewContext}.
 *
 * @author christian.fritz
 */
@NormalScope
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Documented
@Inherited
public @interface ViewScoped {
}
