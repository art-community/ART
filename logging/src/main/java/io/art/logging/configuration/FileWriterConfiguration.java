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
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.context.Context.*;
import static io.art.core.handler.ExceptionHandler.*;
import static io.art.logging.constants.LoggingModuleConstants.ConfigurationKeys.*;
import static io.art.logging.constants.LoggingModuleConstants.Defaults.*;
import static java.time.format.DateTimeFormatter.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;

@Getter
@Builder
public class FileWriterConfiguration {
    private final String prefix;

    @Builder.Default
    private final Path directory = context().configuration().getWorkingDirectory();

    @Builder.Default
    private final String suffix = DEFAULT_LOG_FILE_NAME_SUFFIX;

    @Builder.Default
    private final DateTimeFormatter timestampFormat = DEFAULT_LOG_FILE_TIME_STAMP_FORMAT;

    @Builder.Default
    private final Duration rotationPeriod = DEFAULT_LOG_FILE_ROTATION_PERIOD;

    public static FileWriterConfiguration from(ConfigurationSource source) {
        return FileWriterConfiguration.builder()
                .directory(let(source.getString(DIRECTORY_KEY), Paths::get, context().configuration().getWorkingDirectory()))
                .rotationPeriod(orElse(source.getDuration(ROTATION_PERIOD_KEY), DEFAULT_LOG_FILE_ROTATION_PERIOD))
                .prefix(orElse(source.getString(PREFIX_KEY), context().configuration().getMainModuleId() + DASH))
                .suffix(orElse(source.getString(SUFFIX_KEY), DEFAULT_LOG_FILE_NAME_SUFFIX))
                .timestampFormat(let(source.getString(TIMESTAMP_FORMAT_KEY),
                        pattern -> handleException(ignore -> DEFAULT_LOG_FILE_TIME_STAMP_FORMAT).call(() -> ofPattern(pattern)),
                        DEFAULT_LOG_FILE_TIME_STAMP_FORMAT
                ))
                .build();
    }
}
