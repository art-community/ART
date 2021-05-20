package io.art.core.constants;

import java.util.concurrent.*;
import java.util.function.*;

public interface EmptyFunctions {
    static Runnable emptyRunnable() {
        return () -> {
        };
    }

    static <T> Callable<T> emptyCallable() {
        return () -> null;
    }

    static <T> Consumer<T> emptyConsumer() {
        return (T ignore) -> {
        };
    }

    static <T, U> BiConsumer<T, U> emptyBiConsumer() {
        return (T first, U second) -> {
        };
    }

    static <T, U, R> BiFunction<T, U, R> emptyBiFunction() {
        return (T first, U second) -> null;
    }

    static <T> Supplier<T> emptySupplier() {
        return () -> null;
    }

    static <T> UnaryOperator<T> emptyUnaryOperator() {
        return item -> item;
    }

    static <K, V> Function<K, V> emptyFunction() {
        return (K ignore) -> null;
    }

    static <T> Predicate<T> emptyTruePredicate() {
        return (T ignore) -> true;
    }

    static <T> Predicate<T> emptyFalsePredicate() {
        return (T ignore) -> false;
    }

    FutureTask<?> EMPTY_FUTURE_TASK = new FutureTask<>(emptyCallable());
}
