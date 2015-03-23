// ______________________________________________________________________________
//          Project: stagediver.fx
// ______________________________________________________________________________
//
//       created by: christian.fritz
//    creation date: 06.02.14 17:34
//      description: Mock Implementations for the view container areas.
// ______________________________________________________________________________
//
//        Copyright: (c) QAware GmbH, all rights reserved
// ______________________________________________________________________________

package de.qaware.sdfx.windowmtg.impl;

import javafx.scene.control.Label;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock Implementations for the view container areas.
 */
public class ViewContainerAreaFactoryMockImpl implements ViewContainerAreaFactory {

    @Override
    public ViewArea getInstance(DragNDropManager dragNDropManager) {
        ViewArea ret = mock(ViewArea.class);
        when(ret.getDragNDropManager()).thenReturn(dragNDropManager);
        when(ret.getNode()).then(new Answer<Label>() {
            @Override
            public Label answer(InvocationOnMock invocationOnMock) {
                return new Label("Mock");
            }
        });
        return ret;
    }

    @Override
    public ViewArea getInstance(ViewArea parent, DragNDropManager dragNDropManager) {
        ViewArea ret = mock(ViewArea.class);
        when(ret.getDragNDropManager()).thenReturn(dragNDropManager);
        when(ret.getParent()).thenReturn(parent);
        when(ret.getNode()).then(new Answer<Label>() {
            @Override
            public Label answer(InvocationOnMock invocationOnMock) {
                return new Label("Mock");
            }
        });
        return ret;
    }
}
