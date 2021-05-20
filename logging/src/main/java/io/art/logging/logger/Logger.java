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

package io.art.logging.logger;

import io.art.logging.constants.*;
import static io.art.core.checker.EmptinessChecker.*;

public interface Logger {
    String getName();

    LoggingLevel getLevel();

    void trace(String message);

    void trace(String format, Object... arguments);

    void trace(String message, Throwable error);


    void debug(String message);

    void debug(String format, Object... arguments);

    void debug(String message, Throwable error);


    void info(String message);

    void info(String format, Object... arguments);

    void info(String message, Throwable error);


    void warn(String message);

    void warn(String format, Object... arguments);

    void warn(String message, Throwable error);


    void error(String message);

    void error(String format, Object... arguments);

    void error(String message, Throwable error);


    boolean isErrorEnabled();

    boolean isWarnEnabled();

    boolean isInfoEnabled();

    boolean isDebugEnabled();

    boolean isTraceEnabled();

    default void error(Throwable throwable) {
        error(ifEmpty(throwable.getLocalizedMessage(), throwable.getMessage()), throwable);
    }
}
