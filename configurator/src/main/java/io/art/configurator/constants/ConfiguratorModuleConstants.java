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

package io.art.configurator.constants;

import io.art.core.collection.*;
import io.art.core.source.ConfigurationSource.*;
import lombok.*;
import static io.art.core.factory.ArrayFactory.*;

public interface ConfiguratorModuleConstants {
    String DEFAULT_MODULE_CONFIGURATION_FILE = "module";

    @Getter
    @AllArgsConstructor
    enum ConfigurationSourceType implements ModuleConfigurationSourceType {
        ENVIRONMENT(0),
        PROPERTIES(1),
        RESOURCES_FILE(2),
        CUSTOM_FILE(3),
        ENTITY(4),
        DELEGATE(5);

        private final int order;
    }

    interface ConfiguratorKeys {
        String MODULE_CONFIG_FILES_ENVIRONMENT = "MODULE_CONFIG_FILES";
        String MODULE_CONFIG_FILES_PROPERTY = "module.config.files";
    }

    interface FileConfigurationExtensions {
        String PROPERTIES_EXTENSION = "properties";
        String YAML_EXTENSION = "yaml";
        String YML_EXTENSION = "yml";
        String JSON_EXTENSION = "json";
        String CONF_EXTENSION = "conf";
        String HOCON_EXTENSION = "hocon";
        ImmutableArray<String> FILE_CONFIGURATION_EXTENSIONS = immutableArrayOf(
                YAML_EXTENSION,
                YML_EXTENSION,
                PROPERTIES_EXTENSION,
                JSON_EXTENSION,
                CONF_EXTENSION,
                HOCON_EXTENSION
        );
    }

    interface ExceptionMessages {
        String UNKNOWN_CONFIGURATION_SOURCE_FILE_EXTENSION = "Unknown configuration source file extension: ''{0}''";
        String CONFIGURATION_WAS_NOT_FOUND = "Configuration was not found in section {0}";
    }
}
