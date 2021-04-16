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

package io.art.scheduler.executor.deferred;

import java.time.*;
import java.util.concurrent.*;

public class DeferredExecutorImplementation implements DeferredExecutor {
    private final DeferredEventObserver observer;

    DeferredExecutorImplementation(DeferredExecutorConfiguration configuration) {
        observer = new DeferredEventObserver(configuration);
    }

    @Override
    public <EventResultType> ForkJoinTask<? extends EventResultType> submit(Callable<? extends EventResultType> eventTask, LocalDateTime triggerTime) {
        return observer.addEvent(eventTask, triggerTime);
    }

    @Override
    public ForkJoinTask<?> execute(Runnable task, LocalDateTime triggerTime) {
        return submit(() -> {
            task.run();
            return null;
        }, triggerTime);
    }

    @Override
    public void shutdown() {
        observer.shutdown();
    }

    @Override
    public void clear() {
        observer.clear();
    }
}
