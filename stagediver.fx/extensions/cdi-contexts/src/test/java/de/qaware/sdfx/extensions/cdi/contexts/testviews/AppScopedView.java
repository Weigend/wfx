/*
 * #%L
 * stagediver.fx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 QAware GmbH
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
package de.qaware.sdfx.extensions.cdi.contexts.testviews;

import de.qaware.sdfx.windowmtg.api.Position;
import de.qaware.sdfx.windowmtg.api.View;
import javafx.scene.Parent;

import javax.enterprise.context.ApplicationScoped;

/**
 * A test view implementation that is {@link ApplicationScoped}.
 *
 * @author christian.fritz
 */
@ApplicationScoped
public class AppScopedView implements View {
    @Override
    public String getViewId() {
        return getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return getViewId();
    }

    @Override
    public String getToolTipInfo() {
        return null;
    }

    @Override
    public Position getDefaultPosition() {
        return null;
    }

    @Override
    public Parent getRootNode() {
        return null;
    }

    @Override
    public double getViewAreaSize() {
        return 0;
    }
}
