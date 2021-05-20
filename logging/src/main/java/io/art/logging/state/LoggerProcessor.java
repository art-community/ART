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

package io.art.logging.state;

import io.art.core.collection.*;
import io.art.logging.manager.*;
import io.art.logging.messaging.*;
import io.art.logging.writer.*;
import lombok.*;


@Getter
public class LoggerProcessor {
    private final LoggerConsumer consumer;
    private final LoggerProducer producer;

    public LoggerProcessor(LoggingQueue queue, ImmutableArray<LoggerWriter> writers, LoggerWriter fallbackWriter) {
        consumer = new LoggerConsumer(writers);
        producer = new LoggerProducer(queue, fallbackWriter);
    }
}
