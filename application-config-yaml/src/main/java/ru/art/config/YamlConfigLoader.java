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

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.*;
import static java.lang.System.*;
import static java.util.Objects.isNull;
import static ru.art.config.YamlConfigLoaderConstants.*;
import static ru.art.config.YamlLoadingExceptionMessages.CONFIG_FILE_WAS_NOT_FOUND;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.constants.SystemProperties.*;
import static ru.art.core.wrapper.ExceptionWrapper.*;
import java.io.*;
import java.net.*;

public class YamlConfigLoader {
    private static YAMLMapper YAML_MAPPER = new YAMLMapper();

    static JsonNode loadYamlConfig(String configId) {
        JsonNode node = wrapException(YamlConfigLoader::loadYaml, YamlLoadingException::new);
        return isEmpty(configId) ? node : node.at(SLASH + configId.replace(DOT, SLASH));
    }

    static URL loadYamlConfigUrl() {
        String configFilePath = getProperty(CONFIG_FILE_PATH_PROPERTY);
        File configFile;
        if (isEmpty(configFilePath) || !(configFile = new File(configFilePath)).exists()) {
            return YamlConfigLoader.class.getClassLoader().getResource(DEFAULT_YAML_CONFIG_FILE_NAME);
        }
        try {
            return configFile.toURI().toURL();
        } catch (Throwable throwable) {
            throw new YamlLoadingException(throwable);
        }
    }

    private static JsonNode loadYaml() throws IOException {
        URL url = loadYamlConfigUrl();
        if (isNull(url)) {
            throw new YamlLoadingException(CONFIG_FILE_WAS_NOT_FOUND);
        }

        return YAML_MAPPER.readTree(url);
    }
}