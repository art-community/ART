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

import io.art.core.context.*;
import io.art.core.module.*;
import lombok.*;
import org.apache.logging.log4j.core.async.*;
import static io.art.core.context.Context.*;
import static io.art.logging.LoggingModuleConstants.*;
import static io.art.logging.LoggingModuleConstants.LoggingMessages.*;
import static java.lang.System.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static java.util.Optional.*;
import static java.util.logging.LogManager.*;
import static lombok.AccessLevel.*;
import static org.apache.logging.log4j.core.util.Constants.*;
import static reactor.util.Loggers.*;
import java.net.*;
import java.nio.file.*;
import org.apache.logging.log4j.core.Logger;

@Getter
public class LoggingModule implements StatelessModule<LoggingModuleConfiguration, LoggingModuleConfiguration.Configurator> {
    @Getter(lazy = true, value = PRIVATE)
    private static final StatelessModuleProxy<LoggingModuleConfiguration> loggingModule = context().getStatelessModule(LoggingModule.class.getSimpleName());
    private final String id = LoggingModule.class.getSimpleName();
    private final LoggingModuleConfiguration configuration = new LoggingModuleConfiguration();
    private final LoggingModuleConfiguration.Configurator configurator = new LoggingModuleConfiguration.Configurator(configuration);

    public static StatelessModuleProxy<LoggingModuleConfiguration> loggingModule() {
        return getLoggingModule();
    }

    @Override
    public void onLoad(Context.Service contextService) {
        getLogManager().reset();

        boolean fromFile = ofNullable(configuration.getConfigurationPath())
                .map(Paths::get)
                .map(path -> path.toFile().exists())
                .orElse(false);

        Logger logger = currentLogger();
        if (fromFile) {
            logger.info(format(CONFIGURE_FROM_FILE, configuration.getConfigurationPath()));
            return;
        }

        ClassLoader loader = LoggingModule.class.getClassLoader();
        URL source;
        boolean fromClasspath = nonNull(source = loader.getResource(LOG4J2_YML_FILE)) || nonNull(source = loader.getResource(LOG4J2_YAML_FILE));

        if (fromClasspath) {
            logger.info(format(CONFIGURE_FROM_CLASSPATH, source.getFile()));
        }

        useCustomLoggers(name -> new ReactorLogger(currentLogger(name)));

        if (configuration.getAsynchronous()) {
            logger.info(USE_ASYNCHRONOUS_LOGGING);
            setProperty(LOG4J_CONTEXT_SELECTOR, AsyncLoggerContextSelector.class.getName());
            return;
        }

        logger.info(USE_SYNCHRONOUS_LOGGING);
    }

    @Override
    public void afterReload(Context.Service contextService) {
        getLogManager().reset();
    }

    public Logger currentLogger() {
        return currentLogger(LoggingModule.class.getName());
    }

    public Logger currentLogger(String topic) {
        return new ConfiguredLogger(topic, configuration);
    }

    public static Logger logger() {
        return logger(LoggingModule.class);
    }

    public static Logger logger(Class<?> topicClass) {
        return logger(topicClass.getName());
    }

    public static Logger logger(String topic) {
        return new ConfiguredLogger(topic, loggingModule().configuration());
    }
}
