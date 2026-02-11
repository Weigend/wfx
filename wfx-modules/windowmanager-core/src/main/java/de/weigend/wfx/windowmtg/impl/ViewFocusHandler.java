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

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles the View focus based on the {@link Scene#focusOwnerProperty()}.
 *
 * @author Software-EKG Team
 */
@Singleton
public class ViewFocusHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewFocusHandler.class);

    private final Set<Scene> registeredScenes = new HashSet<>();

    private final MultiWindowManager windowManager;

    private boolean initialized = false;

    /**
     * Init the ViewFocusHandler for the given {@link MultiWindowManager}.
     *
     * @param windowManager Observe this window manager.
     */
    @Inject
    public ViewFocusHandler(MultiWindowManager windowManager) {
        this.windowManager = windowManager;
    }

    /**
     * Initialize the focus handler.
     */
    @PostConstruct
    public void init() {
        if (initialized) {
            return;
        }
        windowManager.mainRootAreaProperty().addListener((observable, oldValue, newValue) -> registerRootArea(newValue));
        windowManager.getRootAreas().addListener((ListChangeListener<RootArea>) c -> {
            if (!c.next()) {
                return;
            }
            c.getAddedSubList().forEach(this::registerRootArea);
            c.getRemoved().forEach(registeredScenes::remove);
        });
        initialized = true;
    }

    /**
     * Register a given {@link RootArea}.
     *
     * @param rootArea The root area to register.
     */
    private void registerRootArea(RootArea rootArea) {
        rootArea.getNode().sceneProperty().addListener(this::registerScene);
    }

    /**
     * Listener to register scenes.
     *
     * @param observable ignored
     * @param oldValue   ignored
     * @param newValue   The scene register.
     */
    @SuppressWarnings("unused")
    private void registerScene(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
        if (newValue == null || registeredScenes.contains(newValue)) {
            return;
        }
        newValue.focusOwnerProperty().addListener(this::focusHandler);
        registeredScenes.add(newValue);
    }

    /**
     * Listener when focus has changed.
     *
     * @param observable ignored
     * @param oldValue   ignored
     * @param newValue   The node which owns the focus.
     */
    @SuppressWarnings("unused")
    private void focusHandler(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
        if (newValue == null) {
            return;
        }
        ViewStatus viewStatus = findView(newValue);
        if (viewStatus != null && windowManager.getFocusedView() != viewStatus.getView()) {
            windowManager.setFocusedView(viewStatus.getView());
            LOGGER.debug("Set current focused view to: {}", viewStatus.getView().getViewId());
        }
    }

    /**
     * Find the {@link ViewStatus} for the given node.
     *
     * @param focusOwner The node which owns the focus.
     * @return The ViewStatus of the given node.
     */
    private ViewStatus findView(Node focusOwner) {
        Node owner = focusOwner;
        while (owner != null) {
            if (owner.getUserData() instanceof TabArea area) {
                TabPane tabPane = (TabPane) area.getNode();
                if (tabPane != null) {
                    Tab tab = tabPane.getSelectionModel().getSelectedItem();
                    if (tab != null) {
                        return (ViewStatus) tab.getUserData();
                    }
                }
            }
            owner = owner.getParent();
        }
        return null;
    }
}
