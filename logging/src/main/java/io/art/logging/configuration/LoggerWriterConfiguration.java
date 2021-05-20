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

import io.art.core.source.*;
import io.art.logging.constants.*;
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.context.Context.*;
import static io.art.core.wrapper.ExceptionWrapper.*;
import static io.art.logging.constants.LoggingModuleConstants.ConfigurationKeys.*;
import static io.art.logging.constants.LoggingModuleConstants.Defaults.*;
import static io.art.logging.constants.LoggingWriterType.*;
import static java.nio.charset.Charset.*;
import java.nio.charset.*;
import java.time.format.*;

@Getter
@Builder(toBuilder = true)
public class LoggerWriterConfiguration {
    @Builder.Default
    private final Charset charset = context().configuration().getCharset();

    @Builder.Default
    private final LoggingWriterType type = CONSOLE;

    @Builder.Default
    private final ConsoleWriterConfiguration console = ConsoleWriterConfiguration.defaults();

    @Builder.Default
    private final FileWriterConfiguration file = FileWriterConfiguration.defaults();

    @Builder.Default
    private final TcpWriterConfiguration tcp = TcpWriterConfiguration.defaults();

    @Builder.Default
    private final UdpWriterConfiguration udp = UdpWriterConfiguration.defaults();

    @Builder.Default
    private final DateTimeFormatter dateTimeFormatter = DEFAULT_LOG_DATE_TIME_FORMAT;

    public static LoggerWriterConfiguration from(ConfigurationSource source, LoggerWriterConfiguration fallback) {
        LoggerWriterConfigurationBuilder builder = LoggerWriterConfiguration.builder();
        builder.type(LoggingWriterType.parse(source.getString(TYPE_KEY), fallback.type));
        builder.console(ConsoleWriterConfiguration.from(source, fallback.console));
        builder.file(FileWriterConfiguration.from(source, fallback.file));
        builder.tcp(TcpWriterConfiguration.from(source, fallback.tcp));
        builder.udp(UdpWriterConfiguration.from(source, fallback.udp));
        builder.dateTimeFormatter(let(source.getString(DATE_TIME_FORMAT_KEY), DateTimeFormatter::ofPattern, fallback.dateTimeFormatter));
        builder.charset(ignoreException(() -> forName(source.getString(CHARSET_KEY)), ignored -> fallback.charset));
        return builder.build();
    }

    public static LoggerWriterConfiguration defaults() {
        return LoggerWriterConfiguration.builder().build();
    }
}
