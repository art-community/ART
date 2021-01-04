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

package ru.art.reactive.service.module;

import lombok.*;
import ru.art.core.module.Module;
import ru.art.core.module.*;
import ru.art.reactive.service.configuration.*;
import static lombok.AccessLevel.PRIVATE;
import static ru.art.core.context.Context.*;
import static ru.art.reactive.service.configuration.ReactiveServiceModuleConfiguration.*;
import static ru.art.reactive.service.constants.ReactiveServiceModuleConstants.*;

@Getter
public class ReactiveServiceModule implements Module<ReactiveServiceModuleConfiguration, ModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private static final ReactiveServiceModuleConfiguration reactiveServiceModule = context().getModule(REACTIVE_SERVICE_MODULE_ID, ReactiveServiceModule::new);
    private final String id = REACTIVE_SERVICE_MODULE_ID;
    private final ReactiveServiceModuleConfiguration defaultConfiguration = DEFAULT_CONFIGURATION;

    public static ReactiveServiceModuleConfiguration reactiveServiceModule() {
        if (contextIsNotReady()) {
            return DEFAULT_CONFIGURATION;
        }
        return getReactiveServiceModule();
    }
}
