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

package io.art.logging.manager;

import io.art.core.extensions.*;
import io.art.logging.configuration.*;
import io.art.logging.messaging.*;
import io.art.logging.model.*;
import io.art.logging.state.*;
import io.art.logging.writer.*;
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.extensions.ThreadExtensions.*;
import static io.art.core.factory.ListFactory.*;
import static io.art.core.factory.MapFactory.*;
import static io.art.logging.constants.LoggingModuleConstants.*;
import static io.art.logging.factory.LoggerWriterFactory.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

@RequiredArgsConstructor
public class LoggingManager {
    private final AtomicBoolean activated = new AtomicBoolean(false);
    private final Map<String, LoggerProcessor> processors = concurrentMap();
    private final Thread consumer = newDaemon(CONSUMER_THREAD, this::processConsuming);
    private final List<Closeable> resources = linkedList();

    private final LoggingQueue queue;
    private final LoggerWriter fallbackWriter;

    public LoggingManager(LoggingModuleConfiguration configuration) {
        queue = new LoggingQueue(configuration);
        fallbackWriter = loggerWriter(this, configuration.getFallbackWriter());
    }

    public boolean isActivated() {
        return activated.get();
    }

    public void activate() {
        if (activated.compareAndSet(false, true)) {
            consumer.start();
        }
    }

    public void deactivate() {
        if (activated.compareAndSet(true, false)) {
            consumer.interrupt();
            resources.forEach(StreamsExtensions::closeQuietly);
        }
    }

    public LoggerProcessor register(LoggerConstructionConfiguration configuration) {
        LoggerProcessor processor = new LoggerProcessor(queue, configuration.getWriters(), fallbackWriter);
        processors.put(configuration.getName(), processor);
        return processor;
    }

    public void register(Closeable resource) {
        resources.add(resource);
    }

    public void remove(Closeable resource) {
        resources.remove(resource);
    }

    private void processConsuming() {
        while (activated.get()) {
            try {
                apply(queue.take(), this::consume);
            } catch (InterruptedException interruptedException) {
                while (!queue.isEmpty()) {
                    apply(queue.poll(), this::consume);
                }
                return;
            }
        }
        while (!queue.isEmpty()) {
            apply(queue.poll(), this::consume);
        }
    }

    private void consume(LoggingMessage message) {
        apply(processors.get(message.getLogger()), processor -> processor.getConsumer().consume(message));
    }
}
