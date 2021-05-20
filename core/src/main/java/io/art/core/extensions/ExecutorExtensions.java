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

package io.art.core.extensions;

import io.art.core.exception.*;
import lombok.experimental.*;
import static io.art.core.constants.ExceptionMessages.*;
import static io.art.core.constants.ThreadConstants.*;
import static io.art.core.wrapper.ExceptionWrapper.*;
import static java.text.MessageFormat.*;
import java.time.*;
import java.util.concurrent.*;

@UtilityClass
public class ExecutorExtensions {
    public void terminate(ExecutorService executorService) {
        terminate(executorService, DEFAULT_EXECUTOR_TERMINATION_TIMEOUT);
    }

    public void terminate(ExecutorService executorService, Duration timeout) {
        if (executorService.isShutdown() || executorService.isTerminated()) return;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new InternalRuntimeException(format(EXECUTOR_SERVICE_WAS_NOT_TERMINATED, timeout));
            }
        } catch (InterruptedException interruptedException) {
            throw new InternalRuntimeException(interruptedException);
        }
    }

    public void terminateQuietly(ExecutorService executorService) {
        terminateQuietly(executorService, DEFAULT_EXECUTOR_TERMINATION_TIMEOUT);
    }

    public void terminateQuietly(ExecutorService executorService, Duration timeout) {
        ignoreException(() -> terminate(executorService, timeout));
    }
}
