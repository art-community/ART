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

package io.art.scheduler.executor.periodic;

import io.art.scheduler.model.*;
import lombok.*;
import static io.art.scheduler.constants.SchedulerModuleConstants.PeriodicTaskMode.*;
import static java.time.LocalDateTime.*;
import java.time.*;
import java.util.concurrent.*;
import java.util.function.*;

@AllArgsConstructor
class RepeatableCallable<T> implements Callable<T> {
    private final Supplier<Boolean> predicate;
    private final Consumer<LocalDateTime> repeat;
    private final PeriodicCallableTask<T> periodicTask;

    @Override
    public T call() {
        if (!predicate.get()) return null;
        LocalDateTime now = periodicTask.getStartTime();
        T result = periodicTask.getDelegate().getAction().apply(periodicTask.getDelegate().getId());
        if (periodicTask.getMode() == FIXED) {
            repeat.accept(now);
            return result;
        }
        if (periodicTask.getMode() == DELAYED) {
            repeat.accept(now());
            return result;
        }
        periodicTask.getDecrement().run();
        return result;
    }
}
