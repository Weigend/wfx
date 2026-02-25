/*
 * #%L
 * wfx is a rich-client-platform for JavaFX.
 * %%
 * Copyright (C) 2013 - 2016 Weigend AM
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
package io.softwareecg.wfx.extension.systemviews;

/**
 * Unit test for {@link ViewOverview}.
 *
 */
// unWith(MockitoJUnitRunner.class)
public class ViewOverviewTest  {

  /*
    @Mock
    private LookupStrategy strategy;
    @Spy
    private ListView<View> views;
    @Mock
    private WindowManager windowManager;
    @InjectMocks
    private ViewOverview overview;
    private SimpleListProperty<View> registeredViews = new SimpleListProperty<>(FXCollections.observableArrayList());

    static {
       // stage = getStage();
    }

    @Before
    public void setUp() throws Exception {
        Lookup.init(strategy);
        when(strategy.lookup(WindowManager.class)).thenReturn(windowManager);
        when(windowManager.getRegisteredViews()).thenReturn(registeredViews);

    }

    @Test
    public void testInitialize() throws Exception {
        overview.initialize(null, null);
        registeredViews.add(mockView("title", true));
        registeredViews.add(mockView("title1", false));

        Thread.sleep(500);
        ListCell<View> cell = find("#overview_title");
        assertThat(cell.getText(), is(equalTo("title")));
        assertThat(cell.getGraphic(), is(notNullValue()));

        cell = find("#overview_title1");
        assertThat(cell.getText(), is(equalTo("title1")));
        assertThat(cell.getGraphic(), is(nullValue()));
    }

    @Test
    public void testShowView() throws Exception {
        overview.initialize(null, null);
        View view = mockView("title", true);
        registeredViews.add(view);
        Thread.sleep(500);

        runInJavaFxThreadAndWait(views::requestFocus);

        type(KeyCode.DOWN);
        type(KeyCode.ENTER);
        verify(windowManager).showView(view);
    }

    private View mockView(String idTitle, boolean image) {
        View mock = mock(View.class);
        if (image) {
            when(mock.getViewImagePath()).thenReturn(getClass().getResource("/io/softwareecg/wfx/extension/uiutils/test.png"));
        }
        when(mock.getTitle()).thenReturn(idTitle);
        when(mock.getViewId()).thenReturn(idTitle);
        return mock;
    }

    @Override
    protected Parent getRootNode() {
        return views;
    }
    */
}