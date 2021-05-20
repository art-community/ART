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
import io.art.core.source.*;
import io.art.logging.constants.*;
import io.art.logging.manager.*;
import io.art.logging.writer.*;
import lombok.Builder;
import lombok.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableArray.*;
import static io.art.logging.constants.LoggingLevel.*;
import static io.art.logging.constants.LoggingModuleConstants.ConfigurationKeys.*;
import java.util.function.*;

@Getter
@Builder(toBuilder = true)
public class LoggerConfiguration {
    @Builder.Default
    private final LoggingLevel level = INFO;

    @Builder.Default
    private final Boolean enabled = true;

    @Builder.Default
    private final ImmutableArray<LoggerWriterConfiguration> configurableWriters = emptyImmutableArray();

    @Builder.Default
    private final ImmutableArray<Function<LoggingManager, LoggerWriter>> customWriters = emptyImmutableArray();

    public static LoggerConfiguration from(ConfigurationSource source, LoggerConfiguration fallback) {
        LoggerConfigurationBuilder builder = LoggerConfiguration.builder();
        builder.level(LoggingLevel.parse(source.getString(LEVEL_KEY), fallback.level));
        builder.enabled(orElse(source.getBool(ENABLED_KEY), fallback.enabled));
        builder.configurableWriters(ifEmpty(
                source.getNestedArray(WRITERS_SECTION, writer -> LoggerWriterConfiguration.from(writer, LoggerWriterConfiguration.defaults())),
                fallback.configurableWriters
        ));
        return builder.build();
    }

    public static LoggerConfiguration defaults() {
        return LoggerConfiguration.builder().build();
    }
}
