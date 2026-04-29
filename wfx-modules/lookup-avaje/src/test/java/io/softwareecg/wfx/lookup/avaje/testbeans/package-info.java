/**
 * Test-only beans for {@link io.softwareecg.wfx.lookup.avaje.AvajeLookupStrategyTest}.
 *
 * <p>The {@code @InjectModule(name="wfxLookupTest")} avoids a name collision with the
 * main module class — without it, Avaje's annotation processor would generate a class
 * named {@code AvajeModule} that shadows {@code io.avaje.inject.spi.AvajeModule}.
 */
@InjectModule(name = "wfxLookupTest")
package io.softwareecg.wfx.lookup.avaje.testbeans;

import io.avaje.inject.InjectModule;
