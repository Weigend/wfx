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
package io.softwareecg.wfx.windowmanager.windows;

import io.softwareecg.wfx.lookup.api.Lookup;
import io.softwareecg.wfx.lookup.api.LookupStrategy;
import io.softwareecg.wfx.windowmanager.api.ShutdownConfirmation;
import javafx.stage.WindowEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the shutdown-confirmation hand-off between
 * {@link DefaultApplicationWindow#platformShutdownRequestHandler}
 * and the looked-up {@link ShutdownConfirmation} strategy.
 * <p>
 * The Platform.exit + System.exit branch is intentionally not exercised
 * here — a System.exit(0) inside a unit test would tear down the JVM
 * and abort the test run. We assert the cancel branch (consumed event)
 * and that the confirmation strategy is consulted.
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultApplicationWindowShutdownTest {

    @Mock
    private LookupStrategy lookupStrategy;

    @Mock
    private ShutdownConfirmation confirmation;

    @Mock
    private WindowEvent event;

    private DefaultApplicationWindow window;

    @Before
    public void setUp() {
        Lookup.init(lookupStrategy);
        when(lookupStrategy.lookup(ShutdownConfirmation.class)).thenReturn(confirmation);
        window = new DefaultApplicationWindow();
        // setStage installs the close-request listener, which we don't
        // exercise — it would require a real Stage. Drive the handler
        // directly instead.
    }

    @Test
    public void cancelledShutdownConsumesTheCloseEvent() {
        when(confirmation.confirm(any())).thenReturn(false);

        window.platformShutdownRequestHandler(event);

        verify(confirmation).confirm(any());
        verify(event).consume();
    }

    @Test
    public void shutdownStrategyIsLookedUpEachTime() {
        // Confirm the handler resolves ShutdownConfirmation per invocation,
        // not at construction time — so an application that registers its
        // own bean late (e.g. via a module's start() phase) is still picked
        // up on the next close attempt.
        when(confirmation.confirm(any())).thenReturn(false);

        window.platformShutdownRequestHandler(event);
        window.platformShutdownRequestHandler(event);

        verify(lookupStrategy, org.mockito.Mockito.times(2)).lookup(ShutdownConfirmation.class);
    }
}
