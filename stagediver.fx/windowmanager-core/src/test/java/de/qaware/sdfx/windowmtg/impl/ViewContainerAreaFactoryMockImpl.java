/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 QAware GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.qaware.sdfx.windowmtg.impl;

import javafx.scene.control.Label;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock Implementations for the view container areas.
 *
 * @author christian.fritz
 */
public class ViewContainerAreaFactoryMockImpl implements ViewContainerAreaFactory {

    private Answer<?> nodeAnswer = invocationOnMock -> new Label("Mock");

    public Answer<?> getNodeAnswer() {
        return nodeAnswer;
    }

    public void setNodeAnswer(Answer<?> nodeAnswer) {
        this.nodeAnswer = nodeAnswer;
    }

    @Override
    public ViewArea getInstance(DragNDropManager dragNDropManager) {
        ViewArea ret = mock(ViewArea.class);
        when(ret.getDragNDropManager()).thenReturn(dragNDropManager);
        when(ret.getNode()).then(nodeAnswer);
        return ret;
    }

    @Override
    public ViewArea getInstance(ViewArea parent, DragNDropManager dragNDropManager) {
        ViewArea ret = mock(ViewArea.class);
        when(ret.getDragNDropManager()).thenReturn(dragNDropManager);
        when(ret.getParent()).thenReturn(parent);
        when(ret.getNode()).then(nodeAnswer);
        return ret;
    }
}
