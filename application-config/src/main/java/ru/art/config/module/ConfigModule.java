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

package ru.art.config.module;

import lombok.*;
import ru.art.config.configuration.*;
import ru.art.config.state.*;
import ru.art.core.module.Module;
import static lombok.AccessLevel.PRIVATE;
import static ru.art.config.configuration.ConfigModuleConfiguration.*;
import static ru.art.config.constants.ConfigModuleConstants.*;
import static ru.art.core.context.Context.*;

@Getter
public class ConfigModule implements Module<ConfigModuleConfiguration, ConfigModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private static final ConfigModuleConfiguration configModule = context().getModule(CONFIG_MODULE_ID, ConfigModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private static final ConfigModuleState configModuleState = context().getModuleState(CONFIG_MODULE_ID, ConfigModule::new);
    private final ConfigModuleConfiguration defaultConfiguration = DEFAULT_CONFIGURATION;
    private final String id = CONFIG_MODULE_ID;
    private final ConfigModuleState state = new ConfigModuleState();

    public static ConfigModuleConfiguration configModule() {
        return getConfigModule();
    }

    public static ConfigModuleState configModuleState() {
        return getConfigModuleState();
    }
}
