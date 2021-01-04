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

package ru.art.logging;

import org.apache.logging.log4j.*;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.*;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.net.*;
import static java.lang.Integer.*;
import static java.lang.System.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static org.apache.logging.log4j.Level.*;
import static org.apache.logging.log4j.LogManager.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.logging.LoggingModuleConstants.*;
import static ru.art.logging.LoggingModuleConstants.LoggingMode.*;
import java.io.*;
import java.util.*;

public interface LoggerConfigurationService {
    static void updateSocketAppender(SocketAppenderConfiguration socketAppenderConfiguration) {
        LoggerContext context = (LoggerContext) getContext(false);
        Configuration configuration = context.getConfiguration();
        if (isNull(configuration)) return;
        SocketAppender socketAppender = configuration.getAppender(SocketAppender.class.getSimpleName());
        context.getRootLogger().removeAppender(socketAppender);
        SocketAppender appender = SocketAppender
                .newBuilder()
                .setName(SocketAppender.class.getSimpleName())
                .withHost(socketAppenderConfiguration.getHost())
                .withPort(socketAppenderConfiguration.getPort())
                .setLayout(socketAppenderConfiguration.getLayout())
                .build();
        appender.start();
        context.getRootLogger().addAppender(appender);
        context.updateLoggers();
    }

    static SocketAppenderConfiguration loadSocketAppenderCurrentConfiguration() {
        LoggerContext context = (LoggerContext) getContext(false);
        Configuration configuration = context.getConfiguration();
        if (isNull(configuration)) return SocketAppenderConfiguration.builder().build();
        SocketAppender socketAppender = configuration.getAppender(SocketAppender.class.getSimpleName());
        if (isNull(socketAppender)) return SocketAppenderConfiguration.builder().build();
        Map<String, String> contentFormat = socketAppender.getManager().getContentFormat();
        String host = contentFormat.get(ADDRESS);
        String protocol = contentFormat.get(PROTOCOL);
        int port = parseInt(contentFormat.get(PORT));
        return SocketAppenderConfiguration.builder()
                .host(host)
                .port(port)
                .protocol(protocol)
                .layout(socketAppender.getLayout())
                .build();
    }

    static ConsoleAppenderConfiguration loadConsoleAppenderConfiguration() {
        LoggerContext context = (LoggerContext) getContext(false);
        Configuration configuration = context.getConfiguration();
        if (isNull(configuration)) return ConsoleAppenderConfiguration.builder().build();
        ConsoleAppender consoleAppender = configuration.getAppender(ConsoleAppender.class.getSimpleName());
        if (isNull(consoleAppender)) return ConsoleAppenderConfiguration.builder().build();
        Layout<?> layout = consoleAppender.getLayout();
        return ConsoleAppenderConfiguration.builder().patternLayout(layout).build();
    }

    static Set<LoggingMode> loadLoggingModes() {
        LoggerConfig rootLogger;
        if (isNull(rootLogger = getRootLogger())) return setOf(CONSOLE);
        List<AppenderRef> appenderRefs = rootLogger.getAppenderRefs();
        if (isEmpty(appenderRefs)) return setOf(CONSOLE);
        return appenderRefs.stream()
                .map(AppenderRef::getRef)
                .filter(Objects::nonNull)
                .map(ref -> ref.equalsIgnoreCase(SocketAppender.class.getSimpleName())
                        ? SOCKET
                        : ref.equalsIgnoreCase(FileAppender.class.getSimpleName())
                        ? FILE
                        : ref.equalsIgnoreCase(RollingFileAppender.class.getSimpleName())
                        ? ROLLING_FILE
                        : CONSOLE)
                .collect(toSet());
    }

    static Level loadLoggingLevel() {
        if (isNull(LoggerConfigurationService.class.getClassLoader().getResource(LOG4J2_YAML_FILE))
                && (isNull(getProperty(LOG4J2_CONFIGURATION_FILE_PROPERTY)) || !new File(getProperty(LOG4J2_CONFIGURATION_FILE_PROPERTY)).exists())) {
            return INFO;
        }
        LoggerConfig rootLogger;
        if (isNull(rootLogger = getRootLogger())) return INFO;
        return getOrElse(rootLogger.getLevel(), INFO);
    }

    static SocketAppender createLoadedSocketAppender() {
        SocketAppenderConfiguration socketAppenderConfiguration = loadSocketAppenderCurrentConfiguration();
        String host = socketAppenderConfiguration.getHost();
        if (isEmpty(host)) return SocketAppender.newBuilder().setName(SocketAppender.class.getSimpleName()).build();
        return SocketAppender
                .newBuilder()
                .setName(SocketAppender.class.getSimpleName())
                .withHost(host)
                .withProtocol(isEmpty(socketAppenderConfiguration) ? Protocol.TCP : Protocol.valueOf(socketAppenderConfiguration.getProtocol().toUpperCase()))
                .withPort(socketAppenderConfiguration.getPort())
                .setLayout(socketAppenderConfiguration.getLayout())
                .build();
    }

    static ConsoleAppender createLoadedConsoleAppender() {
        Layout<?> patternLayout = loadConsoleAppenderConfiguration().getPatternLayout();
        if (isNull(patternLayout)) ConsoleAppender.newBuilder().setName(ConsoleAppender.class.getSimpleName()).build();
        return ConsoleAppender
                .newBuilder()
                .setName(ConsoleAppender.class.getSimpleName())
                .setLayout(patternLayout)
                .build();
    }

    static void setLoggingModes(Set<LoggingMode> modes) {
        LoggerContext context = (LoggerContext) getContext(false);
        Logger rootLogger = context.getRootLogger();
        for (LoggingMode mode : modes) {
            switch (mode) {
                case CONSOLE:
                    ConsoleAppender loadedConsoleAppender = createLoadedConsoleAppender();
                    loadedConsoleAppender.start();
                    rootLogger.getAppenders().values().forEach(rootLogger::removeAppender);
                    rootLogger.getAppenders()
                            .values()
                            .stream()
                            .filter(appender -> ConsoleAppender.class.getSimpleName().equals(appender.getName()))
                            .forEach(rootLogger::removeAppender);
                    rootLogger.addAppender(loadedConsoleAppender);
                    context.updateLoggers();
                    break;
                case SOCKET:
                    SocketAppender loadedSocketAppender = createLoadedSocketAppender();
                    loadedSocketAppender.start();
                    rootLogger.getAppenders()
                            .values()
                            .stream()
                            .filter(appender -> SocketAppender.class.getSimpleName().equals(appender.getName()))
                            .forEach(rootLogger::removeAppender);
                    rootLogger.addAppender(loadedSocketAppender);
                    context.updateLoggers();
            }
        }
    }

    static LoggerConfig getRootLogger() {
        LoggerContext context = (LoggerContext) getContext(false);
        Configuration configuration = context.getConfiguration();
        if (isNull(configuration)) return null;
        return configuration.getRootLogger();
    }
}
