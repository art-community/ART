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

package io.art.configurator.source;

import io.art.configuration.yaml.source.*;
import io.art.configurator.constants.ConfiguratorModuleConstants.*;
import io.art.configurator.exception.*;
import io.art.core.file.*;
import io.art.core.source.*;
import lombok.*;
import lombok.experimental.Delegate;
import static io.art.configurator.constants.ConfiguratorModuleConstants.FileConfigurationExtensions.*;
import static io.art.core.extensions.FileExtensions.*;

public class FileConfigurationSource implements NestedConfiguration {
    @Getter
    private final String section;
    @Getter
    private final String path;
    @Delegate
    private final NestedConfiguration source;

    public FileConfigurationSource(String section, ConfigurationSourceType type, FileProxy file) {
        this.section = section;
        source = selectSource(section, type, file);
        path = file.getPath();
    }

    private static NestedConfiguration selectSource(String section, ConfigurationSourceType type, FileProxy file) {
        String extension = parseExtension(file.getPath());
        ConfigurationSourceParameters parameters = ConfigurationSourceParameters.builder()
                .section(section)
                .type(type)
                .path(file.getPath())
                .inputStream(file.getInputStream())
                .build();
        switch (extension) {
            case HOCON_EXTENSION:
            case JSON_EXTENSION:
            case CONF_EXTENSION:
            case PROPERTIES_EXTENSION:
                return new TypesafeConfigurationSource(parameters);
            case YAML_EXTENSION:
            case YML_EXTENSION:
                return new YamlConfigurationSource(parameters);
        }
        throw new UnknownConfigurationFileExtensionException(extension);
    }
}
