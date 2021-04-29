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

package io.art.logging;

import io.art.core.module.*;
import io.art.core.source.*;
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.logging.LoggingModuleConstants.ConfigurationKeys.*;
import static io.art.logging.LoggingModuleConstants.*;
import static java.lang.System.*;

@Getter
public class LoggingModuleConfiguration implements ModuleConfiguration {
    private Boolean enabled = true;
    private Boolean colored = true;
    private Boolean asynchronous = true;
    private String configurationPath;

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<LoggingModuleConfiguration, Configurator> {
        private final LoggingModuleConfiguration configuration;

        @Override
        public Configurator from(ConfigurationSource source) {
            configuration.enabled = orElse(source.getNested(LOGGING_SECTION, logging -> logging.getBool(ENABLED_KEY)), configuration.enabled);
            configuration.colored = orElse(source.getNested(LOGGING_SECTION, logging -> logging.getBool(COLORED_KEY)), configuration.colored);
            configuration.asynchronous = orElse(source.getNested(LOGGING_SECTION, logging -> logging.getBool(ASYNCHRONOUS_KEY)), configuration.asynchronous);
            configuration.configurationPath = orElse(source.getNested(LOGGING_SECTION, logging -> logging.getString(CONFIGURATION_PATH_KEY)), getProperty(LOG42_CONFIGURATION_FILE_PROPERTY));
            return this;
        }

        @Override
        public Configurator configure(LoggingModuleConfiguration configuration) {
            apply(configuration.getEnabled(), enabled -> this.configuration.enabled = enabled);
            apply(configuration.getColored(), colored -> this.configuration.colored = colored);
            apply(configuration.getAsynchronous(), asynchronous -> this.configuration.asynchronous = asynchronous);
            apply(configuration.getConfigurationPath(), path -> this.configuration.configurationPath = path);
            return this;
        }
    }
}
