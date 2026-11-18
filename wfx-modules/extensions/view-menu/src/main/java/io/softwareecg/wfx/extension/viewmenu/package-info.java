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
/**
 * Auto-built top-level "View" menu with one entry per registered TOOL view.
 *
 * <p>The {@code @InjectModule(name="wfxViewMenu")} names the Avaje-generated module
 * class {@code WfxViewMenuModule}. The default name, derived from the package suffix,
 * would be {@code ViewmenuModule}, which differs from {@link ViewMenuModule} only in
 * case. On Windows and macOS, whose file systems ignore case, the two class files
 * would overwrite each other and the module could not be built.
 *
 * @see io.softwareecg.wfx.extension.viewmenu.ViewMenuModule
 * @since 1.1.0
 */
@InjectModule(name = "wfxViewMenu")
package io.softwareecg.wfx.extension.viewmenu;

import io.avaje.inject.InjectModule;
