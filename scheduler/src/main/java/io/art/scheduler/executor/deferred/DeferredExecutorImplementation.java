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

import lombok.*;
import static io.art.core.constants.ThreadConstants.*;
import static io.art.core.wrapper.FunctionWrapper.*;
import static io.art.scheduler.constants.SchedulerModuleConstants.Defaults.*;
import java.time.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

@Builder
public class DeferredExecutorImplementation implements DeferredExecutor {
    @Getter
    @Builder.Default
    private final ExceptionHandler exceptionHandler = new DefaultExceptionHandler();

    @Getter
    @Builder.Default
    private final int pendingInitialCapacity = DEFAULT_PENDING_INITIAL_CAPACITY;

    @Getter
    @Builder.Default
    private final int poolSize = DEFAULT_SCHEDULER_POOL_SIZE;

    @Getter
    @Builder.Default
    private final Duration taskExecutionTimeout = DEFAULT_TASK_EXECUTION_TIMEOUT;

    @Getter
    @Builder.Default
    private final Duration poolTerminationTimeout = DEFAULT_EXECUTOR_TERMINATION_TIMEOUT;

    @Getter
    @Builder.Default
    private final boolean awaitOnShutdown = true;

    @Getter(lazy = true)
    private final DeferredEventObserver observer = new DeferredEventObserver(this);

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public <EventResultType> Future<? extends EventResultType> submit(Callable<? extends EventResultType> eventTask, LocalDateTime triggerTime) {
        Callable<EventResultType> callable = () -> {
            EventResultType result = eventTask.call();
            counter.decrementAndGet();
            return result;
        };
        return submit(callable, triggerTime, counter.incrementAndGet());
    }

    @Override
    public Future<?> execute(Runnable task, LocalDateTime triggerTime) {
        return submit(wrap(task), triggerTime);
    }

    @Override
    public <EventResultType> Future<? extends EventResultType> submit(Callable<? extends EventResultType> eventTask, LocalDateTime triggerTime, int order) {
        return getObserver().addEvent(eventTask, triggerTime, order);
    }

    @Override
    public Future<?> execute(Runnable task, LocalDateTime triggerTime, int order) {
        return submit(wrap(task), triggerTime, order);
    }

    @Override
    public void shutdown() {
        getObserver().shutdown();
    }

    @Override
    public void clear() {
        getObserver().clear();
    }
}
