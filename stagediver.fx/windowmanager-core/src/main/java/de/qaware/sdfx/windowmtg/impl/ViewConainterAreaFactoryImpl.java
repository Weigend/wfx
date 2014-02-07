// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 06.02.14 17:27
//      description: Produce tabareas
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Implementation for the view container areas. This Implementation will show the added views as tabs within a TabPane.
 */
@Component(immediate = true)
@Service(value = ViewConainterAreaFactory.class)
public class ViewConainterAreaFactoryImpl implements ViewConainterAreaFactory {

    @Override
    public ViewArea getInstance(DragNDropManager dragNDropManager) {
        return new TabArea(dragNDropManager);
    }

    @Override
    public ViewArea getInstance(ViewArea parent, DragNDropManager dragNDropManager) {
        return new TabArea(parent, dragNDropManager);
    }
}
