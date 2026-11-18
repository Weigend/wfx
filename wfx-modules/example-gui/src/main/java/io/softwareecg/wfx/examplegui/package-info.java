/*
 * #%L
 * Example GUI Implementation for wfx
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
/**
 * This is a very small example project.
 *
 * It is placed within the platform codebase because tho it is easier to handle the development.
 *
 * <p>The {@code @InjectModule(name="wfxExampleGui")} names the Avaje-generated module
 * class {@code WfxExampleGuiModule}. The default name, derived from the package suffix,
 * would be {@code ExampleguiModule}, which differs from {@link ExampleGuiModule} only in
 * case. On Windows and macOS, whose file systems ignore case, the two class files
 * would overwrite each other and the module could not be built.
 */
@InjectModule(name = "wfxExampleGui")
package io.softwareecg.wfx.examplegui;

import io.avaje.inject.InjectModule;
