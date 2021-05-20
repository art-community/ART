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

package io.art.logging.factory;

import io.art.logging.configuration.*;
import io.art.logging.logger.*;
import io.art.logging.manager.*;
import io.art.logging.writer.*;
import lombok.experimental.*;
import static io.art.core.collection.ImmutableArray.*;
import static io.art.logging.factory.LoggerWriterFactory.*;

@UtilityClass
public class LoggerFactory {
    public static Logger createLogger(String name, LoggerConfiguration configuration, LoggingManager manager) {
        Builder<LoggerWriter> writers = immutableArrayBuilder();
        configuration
                .getConfigurableWriters()
                .stream()
                .map(writerConfiguration -> loggerWriter(manager, writerConfiguration))
                .forEach(writers::add);

        configuration
                .getCustomWriters()
                .stream()
                .map(writer -> writer.apply(manager))
                .forEach(writers::add);

        LoggerConstructionConfiguration constructionConfiguration = LoggerConstructionConfiguration.builder()
                .name(name)
                .loggerConfiguration(configuration)
                .writers(writers.build())
                .build();

        return new LoggerImplementation(constructionConfiguration, manager.register(constructionConfiguration).getProducer());
    }
}
