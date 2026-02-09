/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2015 Weigend AM
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
package de.weigend.wfx.windowmtg.impl;

import de.weigend.wfx.windowmtg.api.Position;
import de.weigend.wfx.windowmtg.api.View;
import javafx.scene.Parent;
import javafx.scene.control.Label;

/**
 * A generic Testview.
 *
 * @author Software-EKG Team
 */
public class TestView implements View {

    private String id;

    private Position position;
    private Label rootNode;

    public TestView(String id, Position position) {
        this.id = id;
        this.position = position;
        rootNode = new Label("View ID: " + id);
        rootNode.setId(id.toLowerCase());
    }

    @Override
    public String getViewId() {
        return id;
    }

    @Override
    public String getTitle() {
        return id;
    }

    @Override
    public String getToolTipInfo() {
        return id;
    }

    @Override
    public Position getDefaultPosition() {
        return position;
    }

    @Override
    public Parent getRootNode() {
        return rootNode;
    }

    @Override
    public double getViewAreaSize() {
        return 0.5;
    }
}
