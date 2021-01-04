package ru.art.core.lazy;

import lombok.*;
import static java.util.Objects.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.function.*;

@RequiredArgsConstructor
public class LazyLoadingValue<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicReference<T> value = new AtomicReference<>();
    private final Supplier<T> loader;

    public T fastValue() {
        T value;
        if (nonNull(value = this.value.get())) {
            return value;
        }
        if (this.value.compareAndSet(null, value = loader.get())) {
            return value;
        }
        return this.value.get();
    }

    public T safeValue() {
        T value;
        if (nonNull(value = this.value.get())) {
            return value;
        }
        try {
            lock.lock();
            if (this.value.compareAndSet(null, value = loader.get())) {
                return value;
            }
            return this.value.get();
        } finally {
            lock.unlock();
        }
    }

    public static <T> LazyLoadingValue<T> lazyValue(Supplier<T> factory) {
        return new LazyLoadingValue<T>(factory);
    }
}
