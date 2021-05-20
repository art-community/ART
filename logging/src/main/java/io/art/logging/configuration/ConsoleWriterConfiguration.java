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
import static io.art.logging.constants.LoggingModuleConstants.ConfigurationKeys.*;

@Getter
@Builder(toBuilder = true)
public class ConsoleWriterConfiguration {
    @Builder.Default
    private final Boolean colored = true;

    public static ConsoleWriterConfiguration from(ConfigurationSource source, ConsoleWriterConfiguration fallback) {
        ConsoleWriterConfigurationBuilder builder = ConsoleWriterConfiguration.builder();
        builder.colored(orElse(source.getBool(COLORED_KEY), fallback.colored));
        return builder.build();
    }

    public static ConsoleWriterConfiguration defaults() {
        return ConsoleWriterConfiguration.builder().build();
    }
}
