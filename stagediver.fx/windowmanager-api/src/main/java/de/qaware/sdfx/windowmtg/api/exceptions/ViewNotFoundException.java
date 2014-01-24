// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 24.01.14 15:25
//      description: View not found exception.
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.api.exceptions;

import java.io.IOException;

/**
 * Special exception in case of the fxml view is not found.
 */
public class ViewNotFoundException extends IOException {

    /**
     * Default constructor (without message)
     */
    public ViewNotFoundException() {
    }

    /**
     * Only print a message.
     *
     * @param message The message.
     */
    public ViewNotFoundException(String message) {
        super(message);
    }

    /**
     * Print a message and rethrow a other exception as cause.
     *
     * @param message The message.
     * @param cause   The cause.
     */
    public ViewNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Remap a exception as view not found exception.
     *
     * @param cause The previous exception.
     */
    public ViewNotFoundException(Throwable cause) {
        super(cause);
    }
}
