/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2026 Weigend AM
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
package io.softwareecg.wfx.windowmanager.api;

import javafx.scene.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Lifecycle callbacks bound to the root node of a view.
 * <p>
 * Whoever builds a view's content can {@linkplain #bind(Node, Hook) bind} a
 * {@link Hook} to its root node; the {@link WindowManager} then reports when a
 * view with that root node is registered and when it is unregistered for good.
 * The callbacks travel with the node, so they work for every {@link View}
 * implementation and need no reference from the view to the code that created
 * its content.
 * <p>
 * WFX uses this to end the event-bus subscriptions of FXML controllers once
 * their view is gone. Without it the bus would keep the controller — and with
 * it the whole scene graph of the closed view — alive for the lifetime of the
 * application.
 */
public final class ViewLifecycle {

    /**
     * Key of the hook list inside {@link Node#getProperties()}.
     */
    private static final Object HOOKS_KEY = new Object() {
        @Override
        public String toString() {
            return "wfx.viewLifecycleHooks";
        }
    };

    private ViewLifecycle() {
    }

    /**
     * Bind a hook to the given root node.
     *
     * @param root the root node of a view's content; ignored if {@code null}
     * @param hook the callbacks to invoke
     */
    public static void bind(Node root, Hook hook) {
        if (root == null || hook == null) {
            return;
        }
        hooks(root, true).add(hook);
    }

    /**
     * Report that a view with the given root node has been registered.
     * <p>
     * Called by the window manager on every registration, including a
     * re-registration of a view that is already known. Hooks must therefore
     * tolerate repeated calls.
     *
     * @param root the root node of the registered view; may be {@code null}
     */
    public static void attached(Node root) {
        for (Hook hook : snapshot(root)) {
            hook.attached();
        }
    }

    /**
     * Report that a view with the given root node has been unregistered and
     * will not be shown again unless it is registered anew.
     *
     * @param root the root node of the unregistered view; may be {@code null}
     */
    public static void detached(Node root) {
        for (Hook hook : snapshot(root)) {
            hook.detached();
        }
    }

    private static List<Hook> snapshot(Node root) {
        if (root == null || !root.hasProperties()) {
            return List.of();
        }
        List<Hook> hooks = hooks(root, false);
        return hooks == null ? List.of() : List.copyOf(hooks);
    }

    @SuppressWarnings("unchecked")
    private static List<Hook> hooks(Node root, boolean create) {
        Object value = root.getProperties().get(HOOKS_KEY);
        if (value == null && create) {
            value = new ArrayList<Hook>();
            root.getProperties().put(HOOKS_KEY, value);
        }
        return (List<Hook>) value;
    }

    /**
     * Callbacks for the registration state of the view a root node belongs to.
     */
    public interface Hook {

        /**
         * The view has been registered. May be called repeatedly for the same
         * view; implementations must be idempotent.
         */
        void attached();

        /**
         * The view has been unregistered for good. A later
         * {@link #attached()} means the same view instance was registered
         * again.
         */
        void detached();
    }
}
