//  ______________________________________________________________________________
//          Project: stagediver.fx
//           Module: lookup-cdi
//  ______________________________________________________________________________
//
//       created by: christian
//    creation date: 05.04.15 00:02
//      description:
//  ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
//  ______________________________________________________________________________

package de.qaware.sdfx.lookup;

import java.lang.annotation.*;

/**
 * Defines the priority within the returned list of {@link LookupStrategy#lookupAll(Class)}.
 * <p/>
 * The higher the more important. Default value is 0.
 *
 * @author christian.fritz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Documented
public @interface Priority {

    /**
     * The priority.
     *
     * @return The priority for the bean.
     */
    int value() default 0;
}
