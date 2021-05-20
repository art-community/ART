package io.art.core.handler;

import io.art.core.callable.*;
import io.art.core.runnable.*;
import lombok.*;
import lombok.experimental.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.constants.StringConstants.*;
import static java.util.Objects.*;
import static java.util.Optional.*;
import static lombok.AccessLevel.*;
import java.util.*;
import java.util.function.*;

@UtilityClass
public class ExceptionHandler<T> {
    public static <T> String emptyIfException(ExceptionCallable<T> operation) {
        if (isNull(operation)) return EMPTY_STRING;
        try {
            T result = operation.call();
            if (isNull(result)) return EMPTY_STRING;
            return result.toString();
        } catch (Throwable throwable) {
            return EMPTY_STRING;
        }
    }

    public static <T> T throwIfNull(T value, RuntimeException throwable) {
        if (isNull(value)) throw throwable;
        return value;
    }

    public static <T> T throwIfEmpty(T value, RuntimeException throwable) {
        if (isEmpty(value)) throw throwable;
        return value;
    }

    public static ExceptionRunnableWrapper consumeException(Function<Throwable, RuntimeException> wrapper) {
        ExceptionRunnableWrapper runnableWrapper = new ExceptionRunnableWrapper();
        runnableWrapper.wrapper = wrapper;
        return runnableWrapper;
    }

    public static ExceptionRunnableHandler consumeException(Consumer<Throwable> consumer) {
        return new ExceptionRunnableHandler(consumer);
    }

    public static <T> ExceptionCallableWrapper<T> wrapException(Function<Throwable, RuntimeException> wrapper) {
        ExceptionCallableWrapper<T> callableWrapper = new ExceptionCallableWrapper<>();
        callableWrapper.wrapper = wrapper;
        return callableWrapper;
    }

    public static <T> ExceptionCallableHandler<T> handleException(Function<Throwable, T> handler) {
        ExceptionCallableHandler<T> callableHandler = new ExceptionCallableHandler<>();
        callableHandler.handler = handler;
        return callableHandler;
    }

    public static <T> T nullIfException(ExceptionCallable<T> callable) {
        return new ExceptionCallableHandler<T>().orNull().call(callable);
    }

    public static <T> Optional<T> optionalIfException(ExceptionCallable<T> callable) {
        return ofNullable(new ExceptionCallableHandler<T>().orNull().call(callable));
    }

    @AllArgsConstructor(access = PRIVATE)
    public static class ExceptionRunnableHandler {
        private final Consumer<Throwable> consumer;

        public void run(ExceptionRunnable runnable) {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                consumer.accept(throwable);
            }
        }
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class ExceptionCallableHandler<T> {
        private Function<Throwable, T> handler;
        private boolean orNull;
        private Supplier<T> emptyReplacement;

        public ExceptionCallableHandler<T> ifEmpty(T replacement) {
            emptyReplacement = () -> replacement;
            return this;
        }

        public ExceptionCallableHandler<T> ifEmpty(Supplier<T> replacement) {
            emptyReplacement = replacement;
            return this;
        }

        public ExceptionCallableHandler<T> orNull() {
            orNull = true;
            return this;
        }

        public T call(ExceptionCallable<T> callable) {
            try {
                T value = cast(callable.call());
                if (isEmpty(value)) return cast(emptyReplacement);
                return value;
            } catch (Throwable throwable) {
                if (nonNull(handler)) {
                    return cast(handler.apply(throwable));
                }
                if (orNull) {
                    return null;
                }
                throw new RuntimeException(throwable);
            }
        }
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class ExceptionCallableWrapper<T> {
        private Function<Throwable, RuntimeException> wrapper;

        public T call(ExceptionCallable<T> callable) {
            try {
                return cast(callable.call());
            } catch (Throwable throwable) {
                if (nonNull(wrapper)) {
                    throw wrapper.apply(throwable);
                }
                throw new RuntimeException(throwable);
            }
        }
    }

    @NoArgsConstructor(access = PRIVATE)
    public static class ExceptionRunnableWrapper {
        private Function<Throwable, RuntimeException> wrapper;

        public void run(ExceptionRunnable runnable) {
            try {
                runnable.run();
            } catch (Throwable throwable) {
                if (nonNull(wrapper)) {
                    throw wrapper.apply(throwable);
                }
                throw new RuntimeException(throwable);
            }
        }
    }
}
