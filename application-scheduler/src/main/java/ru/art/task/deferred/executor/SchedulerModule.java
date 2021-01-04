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
import ru.art.core.module.Module;
import ru.art.core.module.*;
import static lombok.AccessLevel.*;
import static ru.art.core.context.Context.*;
import static ru.art.task.deferred.executor.SchedulerModuleConfiguration.*;
import static ru.art.task.deferred.executor.SchedulerModuleConstants.*;

@Getter
public class SchedulerModule implements Module<SchedulerModuleConfiguration, ModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private final static SchedulerModuleConfiguration schedulerModule = context().getModule(SCHEDULER_MODULE_ID, SchedulerModule::new);
    private final String id = SCHEDULER_MODULE_ID;
    private final SchedulerModuleConfiguration defaultConfiguration = DEFAULT_CONFIGURATION;

    public static SchedulerModuleConfiguration schedulerModule() {
        if (contextIsNotReady()) {
            return DEFAULT_CONFIGURATION;
        }
        return getSchedulerModule();
    }

    @Override
    public void onUnload() {
        DeferredExecutor deferredExecutor = schedulerModule().getDeferredExecutor();
        PeriodicExecutor periodicExecutor = schedulerModule().getPeriodicExecutor();
        deferredExecutor.clear();
        deferredExecutor.shutdown();
        periodicExecutor.clear();
        periodicExecutor.shutdown();
    }
}
