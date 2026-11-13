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

import io.softwareecg.wfx.windowmanager.impl.LayoutSnapshot.Split;
import io.softwareecg.wfx.windowmanager.impl.LayoutSnapshot.Tabs;
import io.softwareecg.wfx.windowmanager.impl.LayoutSnapshot.Window;
import javafx.geometry.Orientation;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

/**
 * Unit test for the text form of a {@link LayoutSnapshot}.
 */
public class LayoutSnapshotTest {

    private static LayoutSnapshot sample() {
        return new LayoutSnapshot(
                new Split(Orientation.HORIZONTAL, 0.2,
                        new Tabs(false, List.of("explorer", "outline"), "outline"),
                        new Split(Orientation.VERTICAL, 0.75,
                                new Tabs(true, List.of("editor-1", "editor-2"), "editor-2"),
                                new Tabs(false, List.of("console"), "console"))),
                List.of(new Window(100, 150, 640, 480, false, new Tabs(true, List.of("chart"), "chart"))),
                List.of("search"),
                "editor-2");
    }

    @Test
    public void writtenLayoutReadsBackUnchanged() {
        LayoutSnapshot original = sample();

        LayoutSnapshot read = LayoutSnapshot.parse(original.write());

        assertThat(read, is(equalTo(original)));
    }

    @Test
    public void equalLayoutsGiveEqualText() {
        assertThat(sample().write(), is(equalTo(sample().write())));
    }

    @Test
    public void textCarriesNoTimestamp() {
        assertThat(sample().write().contains("#"), is(false));
    }

    @Test
    public void viewIdsWithSpecialCharactersSurvive() {
        List<String> ids = List.of(" leading space", "a=b:c", "line\nbreak", "tab\there",
                "back\\slash", "#hash", "!bang", "ümläut ✓", "");
        LayoutSnapshot original = new LayoutSnapshot(new Tabs(true, ids, "a=b:c"), List.of(), ids, "line\nbreak");

        LayoutSnapshot read = LayoutSnapshot.parse(original.write());

        assertThat(read, is(equalTo(original)));
    }

    @Test
    public void emptyLayoutReadsBack() {
        LayoutSnapshot original = new LayoutSnapshot(new Tabs(true, List.of(), null), List.of(), List.of(), null);

        assertThat(LayoutSnapshot.parse(original.write()), is(equalTo(original)));
    }

    @Test
    public void nullAndBlankTextAreRejected() {
        assertRejected(null, "empty");
        assertRejected("   \n", "empty");
    }

    @Test
    public void textThatIsNoLayoutIsRejected() {
        assertRejected("hello world", "version");
        assertRejected("{\"json\": true}", "version");
    }

    @Test
    public void unknownVersionIsRejected() {
        String text = sample().write().replace("version=1", "version=99");

        assertRejected(text, "version 99");
    }

    @Test
    public void truncatedLayoutIsRejected() {
        String text = sample().write();
        String withoutSecond = text.lines()
                .filter(line -> !line.startsWith("main.second."))
                .reduce("", (a, b) -> a + b + "\n");

        assertRejected(withoutSecond, "main.second.type");
    }

    @Test
    public void invalidValuesAreRejected() {
        assertRejected(sample().write().replace("main.orientation=HORIZONTAL", "main.orientation=DIAGONAL"),
                "main.orientation");
        assertRejected(sample().write().replace("main.divider=0.2", "main.divider=7"), "main.divider");
        assertRejected(sample().write().replace("main.divider=0.2", "main.divider=NaN"), "main.divider");
        assertRejected(sample().write().replace("main.type=split", "main.type=grid"), "main.type");
        assertRejected(sample().write().replace("windows=1", "windows=-1"), "windows");
        assertRejected(sample().write().replace("window.0.width=640.0", "window.0.width=wide"), "window.0.width");
    }

    @Test
    public void absurdListSizeIsRejectedWithoutAllocatingIt() {
        String text = sample().write().replace("hidden.count=1", "hidden.count=2000000000");

        assertRejected(text, "hidden.1");
    }

    private static void assertRejected(String text, String expectedInMessage) {
        try {
            LayoutSnapshot.parse(text);
            fail("expected an IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString(expectedInMessage));
        }
    }
}
