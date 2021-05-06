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

import io.art.scheduler.exception.*;
import lombok.*;
import static io.art.scheduler.constants.SchedulerModuleConstants.ExceptionMessages.*;
import static io.art.scheduler.constants.SchedulerModuleConstants.ExceptionMessages.ExceptionEvent.*;
import static java.util.Objects.*;
import static java.util.concurrent.ForkJoinPool.*;
import static java.util.concurrent.TimeUnit.*;
import java.time.*;
import java.util.concurrent.*;

class DeferredEventObserver {
    private final DeferredExecutorConfiguration configuration;
    private final ForkJoinPool threadPool;
    private final DelayQueue<DeferredEvent<?>> deferredEvents;

    DeferredEventObserver(DeferredExecutorConfiguration configuration) {
        this.configuration = configuration;
        deferredEvents = new DelayQueue<>();
        threadPool = createThreadPool();
        observe();
    }

    <EventResultType> ForkJoinTask<? extends EventResultType> addEvent(Callable<? extends EventResultType> task, LocalDateTime triggerTime) {
        if (deferredEvents.size() + 1 > configuration.getEventsQueueMaxSize()) {
            return forceExecuteEvent(task);
        }
        ForkJoinTask<EventResultType> forkJoinTask = new CallableExecuteAction(task);
        DeferredEvent<EventResultType> event = new DeferredEvent<>(forkJoinTask, triggerTime, deferredEvents.size());
        deferredEvents.add(event);
        return forkJoinTask;
    }

    void shutdown() {
        threadPool.shutdownNow();
        if (!configuration.isAwaitAllTasksTerminationOnShutdown()) {
            return;
        }
        try {
            if (!threadPool.awaitTermination(configuration.getThreadPoolTerminationTimeout(), MILLISECONDS)) {
                configuration.getExceptionHandler().onException(POOL_SHUTDOWN, new SchedulerModuleException(AWAIT_TERMINATION_EXCEPTION));
            }
        } catch (Throwable throwable) {
            configuration.getExceptionHandler().onException(POOL_SHUTDOWN, throwable);
        }
    }

    private void observe() {
        threadPool.execute(this::observeQueue);
    }


    @SuppressWarnings("ConstantConditions")
    private void observeQueue() {
        DeferredEvent<?> currentEvent;
        try {
            while (nonNull(currentEvent = deferredEvents.take())) {
                try {
                    ForkJoinTask<?> task = getTaskFromEvent(currentEvent);
                    if (task.isCancelled()) continue;
                    task = task.fork();
                    DeferredEvent<?> nextEvent;
                    if (nonNull(nextEvent = deferredEvents.peek()) && nextEvent.getTriggerDateTime() == currentEvent.getTriggerDateTime() && !task.isCancelled()) {
                        task.join();
                    }
                } catch (Throwable throwable) {
                    configuration.getExceptionHandler().onException(TASK_EXECUTION, throwable);
                }
            }
        } catch (InterruptedException ignore) {
            // Ignoring exception because interrupting is normal situation when we want shutdown observer
        } catch (Throwable throwable) {
            configuration.getExceptionHandler().onException(TASK_OBSERVING, throwable);
        }
    }

    private ForkJoinTask<?> getTaskFromEvent(DeferredEvent<?> currentEvent) {
        return (ForkJoinTask<?>) currentEvent.getTask();
    }

    private ForkJoinPool createThreadPool() {
        return new ForkJoinPool(configuration.getThreadPoolCoreSize(), defaultForkJoinWorkerThreadFactory, configuration.getThreadPoolExceptionHandler(), true);
    }

    private <EventResultType> ForkJoinTask<? extends EventResultType> forceExecuteEvent(Callable<? extends EventResultType> task) {
        return threadPool.submit(task);
    }

    void clear() {
        deferredEvents.clear();
    }

    @RequiredArgsConstructor
    final class CallableExecuteAction<T> extends ForkJoinTask<T> {
        final Callable<? extends T> callable;
        T result;

        public final T getRawResult() {
            return result;
        }

        public final void setRawResult(T value) {
            result = value;
        }

        public final boolean exec() {
            try {
                result = callable.call();
            } catch (Throwable throwable) {
                DeferredEventObserver.this.configuration.getExceptionHandler().onException(TASK_EXECUTION, throwable);
            }
            return true;
        }
    }
}
