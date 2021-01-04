/*
 * ART Java
 *
 * Copyright 2019 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.art.core.constants;

import static ru.art.core.colorizer.AnsiColorizer.*;

public interface LoggingMessages {
    String CONTEXT_RELOADED_MESSAGE = success("Context has been reloaded in {0}[ms]");
    String CONTEXT_CHANGED = success("Context has been changed. New configuration class is {0}");
    String MODULE_LOADED_MESSAGE = success("Module: ''{0}'' was loaded in {1}[ms] with configuration class {2}");
    String MODULE_OVERRIDDEN_MESSAGE = success("Module: ''{0}'' was overridden in {1}[ms]");
    String MODULE_RELOADED_MESSAGE = success("Module: ''{0}'' was reloaded in {1}[ms]");
    String MODULE_REFRESHED_MESSAGE = success("Module: ''{0}'' was refreshed in {1}[ms]");
    String MODULE_REFRESHED_AND_RELOADED_MESSAGE = success("Module: ''{0}'' was refreshed and reloaded in {1}[ms]");
}
