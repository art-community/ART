/*
 * ART
 *
 * Copyright 2019-2021 ART
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

package io.art.core.constants;

import static io.art.core.colorizer.AnsiColorizer.*;

public interface LoggingMessages {
    String MODULE_LOADED_MESSAGE = success("Module: ''{0}'' was loaded");
    String MODULE_UNLOADED_MESSAGE = success("Module: ''{0}'' was unloaded");
    String MODULE_RELOADING_START_MESSAGE = success("Module: ''{0}'' reloading...");
    String MODULE_RELOADING_END_MESSAGE = success("Module: ''{0}'' reloaded");
}
