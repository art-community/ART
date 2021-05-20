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

package io.art.logging.configuration;

import io.art.core.collection.*;
import io.art.core.module.*;
import io.art.core.source.*;
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.factory.ArrayFactory.*;
import static io.art.logging.constants.LoggingModuleConstants.ConfigurationKeys.*;
import static io.art.logging.constants.LoggingModuleConstants.Defaults.*;

@Getter
public class LoggingModuleConfiguration implements ModuleConfiguration {
    private ImmutableMap<String, LoggerConfiguration> loggers = emptyImmutableMap();

    private final int queueCapacity = DEFAULT_QUEUE_CAPACITY;

    private LoggerConfiguration defaultLogger = LoggerConfiguration.defaults()
            .toBuilder()
            .configurableWriters(immutableArrayOf(LoggerWriterConfiguration.defaults()))
            .build();

    private LoggerWriterConfiguration fallbackWriter = LoggerWriterConfiguration.builder()
            .console(ConsoleWriterConfiguration.builder().build())
            .build();


    public boolean isColored() {
        boolean defaultColored = defaultLogger.getConfigurableWriters()
                .stream()
                .anyMatch(writer -> writer.getConsole().getColored());
        boolean loggersColored = loggers.values()
                .stream()
                .anyMatch(logger -> logger.getConfigurableWriters().stream().anyMatch(writer -> writer.getConsole().getColored()));
        boolean fallbackColored = fallbackWriter.getConsole().getColored();
        return defaultColored || loggersColored || fallbackColored;
    }

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<LoggingModuleConfiguration, Configurator> {
        private final LoggingModuleConfiguration configuration;

        @Override
        public Configurator initialize(LoggingModuleConfiguration configuration) {
            this.configuration.defaultLogger = configuration.getDefaultLogger();
            this.configuration.loggers = configuration.getLoggers();
            this.configuration.fallbackWriter = configuration.getFallbackWriter();
            return this;
        }

        @Override
        public Configurator from(ConfigurationSource source) {
            configuration.defaultLogger = orElse(
                    source.getNested(LOGGING_DEFAULT_SECTION, defaultLogger -> LoggerConfiguration.from(defaultLogger, configuration.defaultLogger)),
                    configuration.defaultLogger
            );
            configuration.fallbackWriter = orElse(
                    source.getNested(LOGGING_FALLBACK_SECTION, fallback -> LoggerWriterConfiguration.from(fallback, configuration.fallbackWriter)),
                    configuration.fallbackWriter
            );
            configuration.loggers = source.getNestedMap(LOGGING_LOGGERS_SECTION, logger -> LoggerConfiguration.from(logger, configuration.defaultLogger));
            return this;
        }
    }
}
