// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 06.02.14 17:24
//      description: Factory to create the view area containers.
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

/**
 * Factory to create the {@link de.qaware.sdfx.windowmtg.impl.ViewArea} that contains only
 * {@link de.qaware.sdfx.windowmtg.api.View}.
 * <p/>
 * For example an implementation will produce {@link de.qaware.sdfx.windowmtg.impl.TabArea}. Thats a ViewArea that will
 * show the added views as tabs within a tabpane.
 */
public interface ViewContainerAreaFactory {
    /**
     * Create a new view area.
     *
     * @param dragNDropManager The drag&drop manager that handles all drag&drop events.
     * @return The created view Area.
     */
    ViewArea getInstance(DragNDropManager dragNDropManager);

    /**
     * Create a new view area.
     *
     * @param parent           The parent view area for this area.
     * @param dragNDropManager The drag&drop manager that handles all drag&drop events.
     * @return The created view Area.
     */
    ViewArea getInstance(ViewArea parent, DragNDropManager dragNDropManager);
}
