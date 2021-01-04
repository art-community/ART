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

package ru.art.config;

import lombok.experimental.*;
import ru.art.config.cache.*;
import ru.art.config.constants.*;
import ru.art.config.exception.*;
import static java.util.Objects.*;
import static ru.art.config.ConfigLoader.*;
import static ru.art.config.constants.ConfigExceptionMessages.*;
import static ru.art.config.constants.ConfigModuleConstants.*;
import static ru.art.config.module.ConfigModule.*;

@UtilityClass
public class ConfigProvider {
    private static final ConfigCacheContainer CONFIG_CACHE_CONTAINER = new ConfigCacheContainer();

    public static String configUrl() {
        return configUrl(configModule().getModuleConfigType());
    }

    public static String configUrl(ConfigType configType) {
        if (isNull(configType)) throw new ConfigException(CONFIG_TYPE_IS_NULL);
        return getConfigUrl(configType);
    }

    public static Config commonConfig() {
        return config(COMMON_CONFIG_ID, configModule().getModuleConfigType());
    }

    public static Config commonConfig(ConfigType configType) {
        return config(COMMON_CONFIG_ID, configType);
    }

    public static Config config(String configId) {
        return config(configId, configModule().getModuleConfigType());
    }

    public static Config config(String configId, ConfigType configType) {
        if (isNull(configId)) throw new ConfigException(CONFIG_ID_IS_NULL);
        if (isNull(configType)) throw new ConfigException(CONFIG_TYPE_IS_NULL);
        if (CONFIG_CACHE_CONTAINER.containsConfig(configId, configType) && CONFIG_CACHE_CONTAINER.configCacheActualized(configId, configType)) {
            return CONFIG_CACHE_CONTAINER.getConfigFromCache(configId, configType);
        }
        return CONFIG_CACHE_CONTAINER.putConfigToCache(configId, configType, loadLocalConfig(configId, configType));
    }
}
