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

import com.typesafe.config.*;
import lombok.experimental.*;
import ru.art.config.constants.*;
import ru.art.config.exception.ConfigException;
import static java.text.MessageFormat.*;
import static ru.art.config.TypesafeConfigLoader.*;
import static ru.art.config.YamlConfigLoader.*;
import static ru.art.config.constants.ConfigExceptionMessages.*;
import static ru.art.config.module.ConfigModule.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.extension.StringExtensions.*;

@UtilityClass
public class ConfigLoader {
    public static String getConfigUrl(ConfigType configType) {
        String currentUrl;
        if (isNotEmpty(currentUrl = configModuleState().localConfigUrl())) {
            return currentUrl;
        }
        switch (configType) {
            case PROPERTIES:
            case JSON:
            case HOCON:
                return configModuleState()
                        .localConfigUrl(emptyIfNull(loadTypeSafeConfigUrl(toTypesafeConfigSyntax(configType))))
                        .localConfigUrl();
            case YAML:
                return configModuleState().localConfigUrl(emptyIfNull(loadYamlConfigUrl())).localConfigUrl();
            default:
                throw new ConfigException(format(UNKNOWN_CONFIG_TYPE, configType));
        }
    }

    public static Config loadLocalConfig(String configId, ConfigType configType) {
        switch (configType) {
            case PROPERTIES:
            case JSON:
            case HOCON:
                configModuleState().localConfigUrl(emptyIfNull(loadTypeSafeConfigUrl(toTypesafeConfigSyntax(configType))));
                return new Config(loadTypeSafeConfig(configId, toTypesafeConfigSyntax(configType)), configType);
            case YAML:
                configModuleState().localConfigUrl(emptyIfNull(loadYamlConfigUrl()));
                return new Config(loadYamlConfig(configId), configType);
            default:
                throw new ConfigException(format(UNKNOWN_CONFIG_TYPE, configType));
        }
    }

    private static ConfigSyntax toTypesafeConfigSyntax(ConfigType configType) {
        switch (configType) {
            case PROPERTIES:
                return ConfigSyntax.PROPERTIES;
            case JSON:
                return ConfigSyntax.JSON;
            case HOCON:
                return ConfigSyntax.CONF;
            default:
                throw new ConfigException(CONFIG_TYPE_IS_NOT_TYPESAFE);
        }
    }
}