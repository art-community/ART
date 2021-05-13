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

package io.art.logging.manager;

import io.art.logging.configuration.*;
import io.art.logging.state.*;
import lombok.*;
import static com.google.common.base.Throwables.*;
import static io.art.scheduler.constants.SchedulerModuleConstants.Defaults.*;
import static java.lang.Thread.*;
import static java.util.concurrent.TimeUnit.*;

@RequiredArgsConstructor
public class LoggingManager {
    private final LoggingModuleConfiguration configuration;
    private final LoggingModuleState state;
    private volatile boolean activated = false;

    public LoggingManager activate() {
        if (activated) return this;
        activated = true;
        configuration.getConsumingExecutor().execute(() -> processConsuming(state));
        return this;
    }

    public LoggingManager deactivate() {
        if (!activated) return this;
        try {
            activated = false;
            configuration.getConsumingExecutor().shutdown();
            if (!configuration.getConsumingExecutor().awaitTermination(DEFAULT_TERMINATION_TIMEOUT.getSeconds(), SECONDS)) {
                return this;
            }
        } catch (Throwable throwable) {
            System.err.println(getStackTraceAsString(throwable));
        }
        return this;
    }

    private void processConsuming(LoggingModuleState state) {
        for (; ; ) {
            if (interrupted()) return;
            if (!activated) {
                while (!state.all(processor -> processor.getQueue().isEmpty())) {
                    state.forEach(processor -> processor.getConsumer().consume());
                }
                return;
            }
            state.forEach(processor -> processor.getConsumer().consume());
        }
    }
}
