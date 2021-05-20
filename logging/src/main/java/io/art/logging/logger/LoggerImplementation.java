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

package io.art.logging.logger;

import io.art.core.collection.*;
import io.art.logging.configuration.*;
import io.art.logging.constants.*;
import io.art.logging.messaging.*;
import io.art.logging.model.*;
import io.art.logging.writer.*;
import lombok.*;
import static io.art.logging.constants.LoggingLevel.*;
import static java.lang.Thread.*;
import static java.text.MessageFormat.*;
import static java.time.LocalDateTime.*;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LoggerImplementation implements Logger {
    @EqualsAndHashCode.Include
    private final String name;
    private final LoggingLevel level;
    private final LoggerConfiguration configuration;
    private final boolean enabled;
    private final boolean errorEnabled;
    private final boolean warnEnabled;
    private final boolean infoEnabled;
    private final boolean debugEnabled;
    private final boolean traceEnabled;
    private final ImmutableArray<LoggerWriter> writers;
    private final LoggerProducer producer;

    public LoggerImplementation(LoggerConstructionConfiguration configuration, LoggerProducer producer) {
        this.name = configuration.getName();
        this.level = configuration.getLoggerConfiguration().getLevel();
        this.configuration = configuration.getLoggerConfiguration();
        this.producer = producer;
        errorEnabled = level.getLevel() >= ERROR.getLevel();
        warnEnabled = level.getLevel() >= WARN.getLevel();
        infoEnabled = level.getLevel() >= INFO.getLevel();
        debugEnabled = level.getLevel() >= DEBUG.getLevel();
        traceEnabled = level.getLevel() >= TRACE.getLevel();
        enabled = configuration.getLoggerConfiguration().getEnabled();
        writers = configuration.getWriters();
    }

    @Override
    public void trace(String message) {
        if (!enabled || !traceEnabled) return;
        producer.produce(createMessage(TRACE, message));
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (!enabled || !traceEnabled) return;
        producer.produce(createMessage(TRACE, format, arguments));
    }

    @Override
    public void trace(String message, Throwable error) {
        if (!enabled || !traceEnabled) return;
        producer.produce(createMessage(TRACE, message, error));
    }

    @Override
    public void debug(String message) {
        if (!enabled || !debugEnabled) return;
        producer.produce(createMessage(DEBUG, message));
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (!enabled || !debugEnabled) return;
        producer.produce(createMessage(DEBUG, format, arguments));
    }

    @Override
    public void debug(String message, Throwable error) {
        if (!enabled || !debugEnabled) return;
        producer.produce(createMessage(DEBUG, message, error));
    }

    @Override
    public void info(String message) {
        if (!enabled || !infoEnabled) return;
        producer.produce(createMessage(INFO, message));
    }

    @Override
    public void info(String format, Object... arguments) {
        if (!enabled || !infoEnabled) return;
        producer.produce(createMessage(INFO, format, arguments));
    }

    @Override
    public void info(String message, Throwable error) {
        if (!enabled || !infoEnabled) return;
        producer.produce(createMessage(INFO, message, error));
    }

    @Override
    public void warn(String message) {
        if (!enabled || !warnEnabled) return;
        producer.produce(createMessage(WARN, message));
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (!enabled || !warnEnabled) return;
        producer.produce(createMessage(WARN, format, arguments));
    }

    @Override
    public void warn(String message, Throwable error) {
        if (!enabled || !warnEnabled) return;
        producer.produce(createMessage(WARN, message, error));
    }

    @Override
    public void error(String message) {
        if (!enabled || !errorEnabled) return;
        producer.produce(createMessage(ERROR, message));
    }

    @Override
    public void error(String format, Object... arguments) {
        if (!enabled || !errorEnabled) return;
        producer.produce(createMessage(ERROR, format, arguments));
    }

    @Override
    public void error(String message, Throwable error) {
        if (!enabled || !errorEnabled) return;
        producer.produce(createMessage(ERROR, message, error));
    }


    private LoggingMessage createMessage(LoggingLevel level, String message) {
        return LoggingMessage.builder()
                .dateTime(now())
                .logger(name)
                .level(level)
                .message(message)
                .thread(currentThread())
                .build();
    }

    private LoggingMessage createMessage(LoggingLevel level, String message, Throwable error) {
        return LoggingMessage.builder()
                .dateTime(now())
                .logger(name)
                .level(level)
                .message(message)
                .thread(currentThread())
                .exception(error)
                .build();
    }

    private LoggingMessage createMessage(LoggingLevel level, String message, Object[] arguments) {
        return LoggingMessage.builder()
                .dateTime(now())
                .logger(name)
                .level(level)
                .thread(currentThread())
                .message(format(message, arguments))
                .build();
    }
}
