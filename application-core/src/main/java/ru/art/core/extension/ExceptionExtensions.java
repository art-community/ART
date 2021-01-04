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

package ru.art.core.extension;

import lombok.experimental.*;
import ru.art.core.callable.*;
import ru.art.core.runnable.*;
import static java.util.Objects.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.StringConstants.*;
import java.util.function.*;

@UtilityClass
public class ExceptionExtensions {
    public static String emptyIfException(ExceptionCallable<String> operation) {
        if (isNull(operation)) return EMPTY_STRING;
        try {
            return operation.call();
        } catch (Throwable throwable) {
            return EMPTY_STRING;
        }
    }

    public static <T> T nullIfException(ExceptionCallable<T> operation) {
        if (isNull(operation)) return null;
        try {
            return operation.call();
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static <T> T ifException(ExceptionCallable<T> operation, T value) {
        requireNonNull(operation);
        try {
            return operation.call();
        } catch (Throwable throwable) {
            return value;
        }
    }

    public static <T> T ifExceptionOrEmpty(ExceptionCallable<T> operation, T value) {
        requireNonNull(operation);
        try {
            T result = operation.call();
            if (isEmpty(result)) return value;
            return result;
        } catch (Throwable throwable) {
            return value;
        }
    }

    public static <T> T exceptionIfNull(T value, RuntimeException throwable) {
        if (isNull(value)) throw throwable;
        return value;
    }

    public static <T> T exceptionIfEmpty(T value, RuntimeException throwable) {
        if (isEmpty(value)) throw throwable;
        return value;
    }

    public static <T> T wrapException(ExceptionCallable<T> action, Function<Throwable, RuntimeException> exceptionFactory) {
        try {
            return action.call();
        } catch (Throwable throwable) {
            throw exceptionFactory.apply(throwable);
        }
    }

    public static <T, R> R doIfException(ExceptionCallable<R> action, Function<Throwable, R> ifException) {
        try {
            return action.call();
        } catch (Throwable throwable) {
            return ifException.apply(throwable);
        }
    }

    public static void doIfException(ExceptionRunnable action, Consumer<Throwable> ifException) {
        try {
            action.run();
        } catch (Throwable throwable) {
            ifException.accept(throwable);
        }
    }


    public static <T> T doIfExceptionOrEmpty(ExceptionCallable<T> operation, Supplier<T> ifEmpty, Function<Throwable, T> ifException) {
        requireNonNull(operation);
        try {
            T result = operation.call();
            if (isEmpty(result)) return ifEmpty.get();
            return result;
        } catch (Throwable throwable) {
            return ifException.apply(throwable);
        }
    }
}
