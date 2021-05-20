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

package io.art.logging.messaging;

import io.art.logging.configuration.*;
import io.art.logging.model.*;
import org.jctools.queues.*;

public class LoggingQueue {
    private final MpscBlockingConsumerArrayQueue<LoggingMessage> queue;

    public LoggingQueue(LoggingModuleConfiguration configuration) {
        queue = new MpscBlockingConsumerArrayQueue<>(configuration.getQueueCapacity());
    }

    public boolean offer(LoggingMessage message) {
        return queue.offer(message);
    }

    public LoggingMessage take() throws InterruptedException {
        return queue.take();
    }

    public LoggingMessage poll() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
