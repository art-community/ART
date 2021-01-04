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
import static com.typesafe.config.ConfigFactory.*;
import static com.typesafe.config.ConfigParseOptions.*;
import static java.lang.System.*;
import static java.nio.file.Paths.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static ru.art.config.TypesafeConfigLoaderConstants.*;
import static ru.art.config.TypesafeConfigLoadingExceptionMessages.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.SystemProperties.*;
import static ru.art.core.extension.FileExtensions.*;
import static ru.art.core.wrapper.ExceptionWrapper.*;
import java.io.*;
import java.net.*;

class TypesafeConfigLoader {
    static ConfigObject loadTypeSafeConfig(String configId, ConfigSyntax configSyntax) {
        Config config = parseReader(wrapException(() -> loadConfigReader(configSyntax),
                TypesafeConfigLoadingException::new), defaults().setSyntax(configSyntax));
        return isEmpty(configId) ? config.root() : config.getObject(configId);
    }

    static URL loadTypeSafeConfigUrl(ConfigSyntax configSyntax) {
        String configFilePath = getProperty(CONFIG_FILE_PATH_PROPERTY);
        File configFile;
        if (isEmpty(configFilePath) ||
                !(configFile = new File(configFilePath)).exists() ||
                isEmpty(readFile(get(configFile.getAbsolutePath())))) {
            return TypesafeConfigLoader.class
                    .getClassLoader()
                    .getResource(format(DEFAULT_TYPESAFE_CONFIG_FILE_NAME, configSyntax.toString().toLowerCase()));
        }
        try {
            return configFile.toURI().toURL();
        } catch (Throwable throwable) {
            throw new TypesafeConfigLoadingException(throwable);
        }
    }

    private static Reader loadConfigReader(ConfigSyntax configSyntax) throws IOException {
        URL url = loadTypeSafeConfigUrl(configSyntax);
        if (isNull(url)) {
            throw new TypesafeConfigLoadingException(format(CONFIG_FILE_NOT_FOUND, configSyntax.toString().toLowerCase()));
        }
        return new InputStreamReader(url.openStream());
    }
}