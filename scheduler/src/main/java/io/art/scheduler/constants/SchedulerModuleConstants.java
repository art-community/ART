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

package io.art.scheduler.constants;

import lombok.*;
import static io.art.core.constants.ThreadConstants.*;
import static java.lang.Math.*;
import static java.time.Duration.*;
import java.time.*;

public interface SchedulerModuleConstants {
    String REFRESHER_TASK = "REFRESHER_TASK";
    String SCHEDULER_NAME = "scheduler";
    String SCHEDULER_THREAD_NAME = "thread";

    enum PeriodicTaskMode {
        FIXED,
        DELAYED
    }

    interface Defaults {
        int DEFAULT_PENDING_INITIAL_CAPACITY = 11;
        int DEFAULT_SCHEDULER_POOL_SIZE = (int) max(ceil(DEFAULT_THREAD_POOL_SIZE * 0.25), 2);
        Duration DEFAULT_TASK_EXECUTION_TIMEOUT = ofMinutes(1);
    }

    interface ConfigurationKeys {
        String CONFIGURATOR_REFRESH_DURATION = "configurator.refresh.duration";
    }

    interface ExceptionMessages {
        String EXCEPTION_OCCURRED_DURING = "Exception occurred during {0} on thread {1}: {2}";
        String AWAIT_TERMINATION_EXCEPTION = "Await termination failed";
        String SCHEDULER_TERMINATED = "Scheduler terminated";
        String REJECTED_EXCEPTION = "Thread pool rejected task";

        @Getter
        @AllArgsConstructor
        enum ExceptionEvent {
            TASK_EXECUTION("task execution"),
            TASK_OBSERVING("task observing"),
            POOL_SHUTDOWN("pool shutdown");

            private final String message;
        }
    }

}
