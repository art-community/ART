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
import java.lang.Thread.*;

@Getter
@Builder
class DeferredExecutorConfiguration {
    private final ExceptionHandler exceptionHandler;
    private final UncaughtExceptionHandler threadPoolExceptionHandler;
    private final int eventsQueueMaxSize;
    private final int threadPoolCoreSize;
    private final boolean awaitAllTasksTerminationOnShutdown;
    private final long threadPoolTerminationTimeout;
    private final boolean shutdownOnExit;
}
