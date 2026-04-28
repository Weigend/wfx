/**
 * Avaje-Inject backed implementation of {@link io.softwareecg.wfx.lookup.LookupStrategy}.
 *
 * <p>The {@code @InjectModule(name="wfxLookup")} avoids a name collision between the
 * Avaje-generated module class (default name derived from package suffix would be
 * {@code AvajeModule}) and Avaje's own {@code io.avaje.inject.spi.AvajeModule} SPI
 * interface.
 */
@InjectModule(name = "wfxLookup")
package io.softwareecg.wfx.lookup.avaje;

import io.avaje.inject.InjectModule;
