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
import java.util.function.*;

@AllArgsConstructor
class RepeatableRunnable implements Runnable {
    private final Supplier<Boolean> predicate;
    private final Consumer<LocalDateTime> repeat;
    private final PeriodicRunnableTask periodicTask;

    @Override
    public void run() {
        if (!predicate.get()) return;
        LocalDateTime now = periodicTask.getStartTime();
        periodicTask.getDelegate().getAction().accept(periodicTask.getDelegate().getId());
        if (periodicTask.getMode() == FIXED) {
            repeat.accept(now);
            return;
        }
        if (periodicTask.getMode() == DELAYED) {
            repeat.accept(now());
            return;
        }
        periodicTask.getDecrement().run();
    }
}
