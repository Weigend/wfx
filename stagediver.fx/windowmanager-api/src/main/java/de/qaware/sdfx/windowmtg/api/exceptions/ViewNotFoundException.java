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
    public ViewNotFoundException() {
    }

    public ViewNotFoundException(String message) {
        super(message);
    }

    public ViewNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ViewNotFoundException(Throwable cause) {
        super(cause);
    }
}
