package io.art.core.property;

import io.art.core.annotation.*;
import static io.art.core.property.DisposableProperty.*;
import static java.util.Objects.*;
import java.util.function.*;

@UsedByGenerator
public class LazyProperty<T> implements Supplier<T> {
    private final DisposableProperty<T> disposable;

    public LazyProperty(Supplier<T> loader) {
        disposable = disposable(loader);
    }


    public LazyProperty<T> initialize() {
        disposable.get();
        return this;
    }

    public boolean initialized() {
        return disposable.initialized();
    }

    public LazyProperty<T> created(Consumer<T> consumer) {
        disposable.created(consumer);
        return this;
    }

    public LazyProperty<T> initialized(Consumer<T> consumer) {
        disposable.initialized(consumer);
        return this;
    }


    @Override
    public T get() {
        return disposable.get();
    }

    public T value() {
        return disposable.value();
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (isNull(other)) {
            return false;
        }
        if (!(other instanceof LazyProperty)) {
            return false;
        }
        return get().equals(((LazyProperty<?>) other).get());
    }

    public static <T> LazyProperty<T> lazy(Supplier<T> factory) {
        return new LazyProperty<>(factory);
    }
}
