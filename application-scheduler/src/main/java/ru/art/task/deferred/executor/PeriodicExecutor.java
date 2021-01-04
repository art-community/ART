/*
 * ART Java
 *
 * Copyright 2019 ART
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

package ru.art.task.deferred.executor;


import lombok.*;
import static java.time.LocalDateTime.*;
import static java.util.Objects.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.factory.CollectionsFactory.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

@RequiredArgsConstructor
public class PeriodicExecutor {
    private final Map<String, Future<?>> executingTasks = concurrentHashMap();
    private final DeferredExecutor deferredExecutor;

    public <EventResultType> Future<? extends EventResultType> submitPeriodic(CallableTask<EventResultType> task, LocalDateTime startTime, Duration duration) {
        Future<?> future = executingTasks.get(task.getId());
        if (nonNull(future)) {
            return cast(future);
        }
        return submit(task, startTime, duration);
    }

    public <EventResultType> Future<? extends EventResultType> submitPeriodic(CallableTask<EventResultType> task, Duration duration) {
        Future<?> future = executingTasks.get(task.getId());
        if (nonNull(future)) {
            return cast(future);
        }
        return submit(task, duration);
    }

    public Future<?> executePeriodic(RunnableTask task, LocalDateTime startTime, Duration duration) {
        Future<?> future = executingTasks.get(task.getId());
        if (nonNull(future)) {
            return cast(future);
        }
        return execute(task, startTime, duration);
    }

    public Future<?> executePeriodic(RunnableTask task, Duration duration) {
        Future<?> future = executingTasks.get(task.getId());
        if (nonNull(future)) {
            return cast(future);
        }
        return execute(task, duration);
    }

    public boolean cancelPeriodicTask(String taskId) {
        return removePeriodicTask(taskId).map(future -> future.cancel(true)).orElse(false);
    }

    public Optional<Future<?>> removePeriodicTask(String taskId) {
        return Optional.ofNullable(executingTasks.remove(taskId));
    }

    public void clear() {
        executingTasks.clear();
        if (nonNull(deferredExecutor)) {
            deferredExecutor.clear();
        }
    }

    public void shutdown() {
        if (nonNull(deferredExecutor)) {
            deferredExecutor.shutdown();
        }
    }

    private <EventResultType> Future<? extends EventResultType> submit(CallableTask<EventResultType> task, LocalDateTime startTime, Duration duration) {
        NotifiedCallable<EventResultType> eventTask = new NotifiedCallable<>(() -> task.getCallable().apply(task.getId()), (notification) -> submitAgain(task, duration));
        Future<? extends EventResultType> future = deferredExecutor.submit(eventTask, startTime);
        executingTasks.put(task.getId(), future);
        return future;
    }

    private <EventResultType> Future<? extends EventResultType> submit(CallableTask<EventResultType> task, Duration duration) {
        NotifiedCallable<EventResultType> eventTask = new NotifiedCallable<>(() -> task.getCallable().apply(task.getId()), (notification) -> submitAgain(task, duration));
        Future<? extends EventResultType> future = deferredExecutor.submit(eventTask);
        executingTasks.put(task.getId(), future);
        return future;
    }

    private Future<?> execute(RunnableTask task, LocalDateTime startTime, Duration duration) {
        Future<?> future = deferredExecutor.execute(new NotifiedRunnable(() -> task.getRunnable().accept(task.getId()), () -> executeAgain(task, duration)), startTime);
        executingTasks.put(task.getId(), future);
        return future;
    }

    private Future<?> execute(RunnableTask task, Duration duration) {
        Future<?> future = deferredExecutor.execute(new NotifiedRunnable(() -> task.getRunnable().accept(task.getId()), () -> executeAgain(task, duration)));
        executingTasks.put(task.getId(), future);
        return future;
    }


    private void submitAgain(CallableTask<?> eventTask, Duration duration) {
        if (executingTasks.containsKey(eventTask.getId())) {
            submit(eventTask, now().plus(duration), duration);
        }
    }

    private void executeAgain(RunnableTask task, Duration duration) {
        if (executingTasks.containsKey(task.getId())) {
            execute(task, now().plus(duration), duration);
        }
    }
}