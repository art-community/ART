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

package ru.art.config.remote.constants;

public interface RemoteConfigLoaderConstants {
    String MODULE_ID_IS_EMPTY = "Module id is empty";
    String CONFIGURATOR_CONNECTION_PROPERTIES_NOT_EXISTS = "Configurator host, port or path was not found in local config or has invalid path. Using local file configuration mode";
    String CONFIGURATION_MODE = "Configuration mode is ''{0}''";
    String CONFIGURATION_FILE_URL = "Configuration file URL is ''{0}''";
    String CONFIGURATION_FILE_NOT_EXISTS = "Configuration file does not exists so using default hard-coded configuration. For file configurations specify -Dmodule.config.file property or add module-config.*configuration type* inside classpath";
    String REMOTE_CONFIGURATION_PROPERTIES = "Remote configuration properties are: ''{0}''";
    String REMOTE_CONFIGURATION_IS_EMPTY = "Remote configuration is empty or configurator was not started. Using local file configuration mode";

    interface LocalConfigKeys {
        String CONFIGURATOR_HOST = "configurator.host";
        String CONFIGURATOR_PORT = "configurator.port";
        String CONFIGURATOR_PATH = "configurator.path";
    }
}
