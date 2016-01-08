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
package de.qaware.sdfx.extension.uiutils;

import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.qaware.sdfx.extension.uiutils.MenuUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

/**
 * Unit test for the {@link MenuUtil}
 *
 * @author christian.fritz
 */
public class MenuUtilTest {

    @Test
    public void testCreateAndFindMenu() throws Exception {
        Menu menu2 = createMenu("menu2", "File");
        List<MenuItem> items = Arrays.asList(createMenu("menu1", "File"), menu2, createMenuItem("item2", "test", null));
        MenuItem item1 = createMenuItem("item1", "Open", null);
        menu2.getItems().add(item1);

        assertThat(findItem(items, "item1", false), is(nullValue()));
        assertThat(findItem(items, "item1", true), is(sameInstance(item1)));
    }

    @Test(expected = NullPointerException.class)
    public void testFindItemParentMenuNull() throws Exception {
        findItem(null, "", false);
    }

    @Test(expected = NullPointerException.class)
    public void testFindItemIdNull() throws Exception {
        findItem(new ArrayList<>(), null, false);
    }

    @Test
    public void testCreateMenuItem() throws Exception {
        MenuItem item = createMenuItem("id", "text", e -> assertThat(e, is(notNullValue())));
        assertThat(item.getId(), is(equalTo("id")));
        assertThat(item.getText(), is(equalTo("text")));
        item.getOnAction().handle(mock(ActionEvent.class));
    }

    @Test
    public void testFindOrCreateItem() throws Exception {
        Menu menu2 = createMenu("menu2", "File");
        List<MenuItem> items = new ArrayList<>(Arrays.asList(createMenu("menu1", "File"), menu2, createMenuItem("item2", "test", null)));

        assertThat(findOrCreateItem(items, "menu2", () -> new Menu("File"), 2), is(sameInstance(menu2)));
        MenuItem item = findOrCreateItem(items, "menu3", () -> new Menu("File"), 2);
        assertThat(item, notNullValue());
        assertThat(items, hasItem(item));
        assertThat(items.get(2), is(sameInstance(item)));
    }
}