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

import io.art.logging.logger.*;
import lombok.*;
import static com.google.common.base.Throwables.*;
import static io.art.logging.module.LoggingModule.*;
import static io.art.scheduler.constants.SchedulerModuleConstants.*;
import static io.art.scheduler.constants.SchedulerModuleConstants.ExceptionMessages.*;
import static java.text.MessageFormat.*;

public class DefaultExceptionHandler implements ExceptionHandler {
    @Getter(lazy = true)
    private final static Logger logger = logger(DefaultExceptionHandler.class);

    @Override
    public void onException(Thread thread, ExceptionMessages.ExceptionEvent event, Throwable throwable) {
        String message = format(EXCEPTION_OCCURRED_DURING, event.getMessage(), thread.getName(), getStackTraceAsString(throwable));
        getLogger().error(message);
    }
}
