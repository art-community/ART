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

package io.art.scheduler.module;

import io.art.core.module.*;
import io.art.core.source.*;
import io.art.scheduler.executor.deferred.*;
import io.art.scheduler.executor.periodic.*;
import lombok.*;
import static io.art.scheduler.constants.SchedulerModuleConstants.ConfigurationKeys.*;
import static io.art.scheduler.executor.deferred.DeferredExecutor.*;
import java.time.*;

@Getter
public class SchedulerModuleConfiguration implements ModuleConfiguration {
    private final DeferredExecutor deferredExecutor = deferredExecutor()
            .exceptionHandler(new DefaultExceptionHandler())
            .build();
    private final PeriodicExecutor periodicExecutor = new PeriodicExecutor(deferredExecutor()
            .exceptionHandler(new DefaultExceptionHandler())
            .build());
    private Duration refreshDuration;

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<SchedulerModuleConfiguration, Configurator> {
        private final SchedulerModuleConfiguration configuration;

        @Override
        public Configurator from(ConfigurationSource source) {
            configuration.refreshDuration = source.getDuration(CONFIGURATOR_REFRESH_DURATION);
            return this;
        }
    }
}
