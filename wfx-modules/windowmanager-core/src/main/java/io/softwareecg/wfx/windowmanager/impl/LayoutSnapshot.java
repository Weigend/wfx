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
package io.softwareecg.wfx.windowmanager.impl;

import javafx.geometry.Orientation;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * The arrangement of the views at one point in time, detached from the scene
 * graph: which views share a tab pane, how the panes are split, where the
 * dividers sit, and which views live in windows of their own.
 * <p>
 * A snapshot refers to views by id only. It is what
 * {@link io.softwareecg.wfx.windowmanager.api.WindowManager#saveLayout()}
 * writes and {@code restoreLayout} reads; the text form is a set of
 * {@code key=value} lines in the syntax of {@link Properties}, with the path
 * through the area tree as key:
 * <pre>
 * version=1
 * main.type=split
 * main.orientation=HORIZONTAL
 * main.divider=0.2
 * main.first.type=tabs
 * main.first.view.0=explorer
 * ...
 * </pre>
 *
 * @param main    the area tree of the main window
 * @param windows the detached windows, each with its own area tree
 * @param hidden  ids of the views that were registered but not shown
 * @param focused id of the view that had the focus, or {@code null}
 */
record LayoutSnapshot(Area main, List<Window> windows, List<String> hidden, String focused) {

    /**
     * Version of the text format. Bumped when an older reader could no longer
     * make sense of what a newer writer produces.
     */
    static final int VERSION = 1;

    private static final String TYPE_SPLIT = "split";
    private static final String TYPE_TABS = "tabs";

    /**
     * A node of the area tree.
     */
    sealed interface Area permits Split, Tabs {
    }

    /**
     * Two areas side by side or on top of each other.
     *
     * @param orientation how the two areas are arranged
     * @param divider     position of the divider, between 0 and 1
     * @param first       the left or upper area
     * @param second      the right or lower area
     */
    record Split(Orientation orientation, double divider, Area first, Area second) implements Area {
    }

    /**
     * A tab pane.
     *
     * @param editor   whether this is the pane that receives the views added
     *                 at {@code Position.CENTER}
     * @param views    ids of the views, in tab order
     * @param selected id of the view whose tab is selected, or {@code null}
     */
    record Tabs(boolean editor, List<String> views, String selected) implements Area {
    }

    /**
     * A detached window.
     *
     * @param x         horizontal screen position
     * @param y         vertical screen position
     * @param width     window width
     * @param height    window height
     * @param maximized whether the window was maximized
     * @param root      the area tree of the window
     */
    record Window(double x, double y, double width, double height, boolean maximized, Area root) {
    }

    /**
     * Write the snapshot as text.
     *
     * @return the text form, one {@code key=value} pair per line, sorted by key
     */
    String write() {
        Properties properties = new Properties();
        properties.setProperty("version", Integer.toString(VERSION));
        writeArea(properties, "main", main);
        properties.setProperty("windows", Integer.toString(windows.size()));
        for (int i = 0; i < windows.size(); i++) {
            Window window = windows.get(i);
            String prefix = "window." + i;
            properties.setProperty(prefix + ".x", Double.toString(window.x()));
            properties.setProperty(prefix + ".y", Double.toString(window.y()));
            properties.setProperty(prefix + ".width", Double.toString(window.width()));
            properties.setProperty(prefix + ".height", Double.toString(window.height()));
            properties.setProperty(prefix + ".maximized", Boolean.toString(window.maximized()));
            writeArea(properties, prefix + ".root", window.root());
        }
        writeList(properties, "hidden", hidden);
        if (focused != null) {
            properties.setProperty("focused", focused);
        }
        StringWriter writer = new StringWriter();
        try {
            properties.store(writer, null);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);   // a StringWriter does not fail
        }
        // Properties adds a timestamp comment and writes in hash order; drop
        // the one and sort the other so that equal layouts give equal text.
        return writer.toString().lines()
                .filter(line -> !line.startsWith("#"))
                .sorted()
                .collect(Collectors.joining("\n", "", "\n"));
    }

    private static void writeArea(Properties properties, String prefix, Area area) {
        if (area instanceof Split split) {
            properties.setProperty(prefix + ".type", TYPE_SPLIT);
            properties.setProperty(prefix + ".orientation", split.orientation().name());
            properties.setProperty(prefix + ".divider", Double.toString(split.divider()));
            writeArea(properties, prefix + ".first", split.first());
            writeArea(properties, prefix + ".second", split.second());
        }
        else if (area instanceof Tabs tabs) {
            properties.setProperty(prefix + ".type", TYPE_TABS);
            properties.setProperty(prefix + ".editor", Boolean.toString(tabs.editor()));
            writeList(properties, prefix + ".view", tabs.views());
            if (tabs.selected() != null) {
                properties.setProperty(prefix + ".selected", tabs.selected());
            }
        }
    }

    private static void writeList(Properties properties, String prefix, List<String> values) {
        properties.setProperty(prefix + ".count", Integer.toString(values.size()));
        for (int i = 0; i < values.size(); i++) {
            properties.setProperty(prefix + "." + i, values.get(i));
        }
    }

    /**
     * Read a snapshot from its text form.
     *
     * @param text what {@link #write()} produced
     * @return the snapshot
     * @throws IllegalArgumentException if the text is not a layout this
     *                                  version can read
     */
    static LayoutSnapshot parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("The layout is empty");
        }
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(text));
        }
        catch (IOException e) {
            throw new IllegalArgumentException("The layout is not readable", e);
        }
        int version = intValue(properties, "version");
        if (version != VERSION) {
            throw new IllegalArgumentException("Unsupported layout version " + version);
        }
        Area main = readArea(properties, "main");
        List<Window> windows = new ArrayList<>();
        int windowCount = intValue(properties, "windows");
        for (int i = 0; i < windowCount; i++) {
            String prefix = "window." + i;
            windows.add(new Window(
                    doubleValue(properties, prefix + ".x"),
                    doubleValue(properties, prefix + ".y"),
                    doubleValue(properties, prefix + ".width"),
                    doubleValue(properties, prefix + ".height"),
                    Boolean.parseBoolean(value(properties, prefix + ".maximized")),
                    readArea(properties, prefix + ".root")));
        }
        return new LayoutSnapshot(main, windows, readList(properties, "hidden"), properties.getProperty("focused"));
    }

    private static Area readArea(Properties properties, String prefix) {
        String type = value(properties, prefix + ".type");
        if (TYPE_SPLIT.equals(type)) {
            Orientation orientation;
            try {
                orientation = Orientation.valueOf(value(properties, prefix + ".orientation"));
            }
            catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid value for " + prefix + ".orientation", e);
            }
            double divider = doubleValue(properties, prefix + ".divider");
            if (!(divider >= 0 && divider <= 1)) {
                throw new IllegalArgumentException("Invalid value for " + prefix + ".divider: " + divider);
            }
            return new Split(orientation, divider,
                    readArea(properties, prefix + ".first"),
                    readArea(properties, prefix + ".second"));
        }
        if (TYPE_TABS.equals(type)) {
            return new Tabs(
                    Boolean.parseBoolean(value(properties, prefix + ".editor")),
                    readList(properties, prefix + ".view"),
                    properties.getProperty(prefix + ".selected"));
        }
        throw new IllegalArgumentException("Invalid value for " + prefix + ".type: " + type);
    }

    private static List<String> readList(Properties properties, String prefix) {
        int count = intValue(properties, prefix + ".count");
        List<String> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(value(properties, prefix + "." + i));
        }
        return values;
    }

    private static String value(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("The layout lacks the entry " + key);
        }
        return value;
    }

    private static int intValue(Properties properties, String key) {
        try {
            int value = Integer.parseInt(value(properties, key));
            if (value < 0) {
                throw new NumberFormatException("negative");
            }
            return value;
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid value for " + key, e);
        }
    }

    private static double doubleValue(Properties properties, String key) {
        try {
            double value = Double.parseDouble(value(properties, key));
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new NumberFormatException("not finite");
            }
            return value;
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid value for " + key, e);
        }
    }
}
