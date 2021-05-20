package io.art.core.property;

import io.art.core.exception.*;
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.constants.ExceptionMessages.*;
import static io.art.core.extensions.CollectionExtensions.*;
import static io.art.core.factory.ListFactory.*;
import static io.art.core.factory.QueueFactory.*;
import static java.util.Objects.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

@RequiredArgsConstructor
public class DisposableProperty<T> implements Supplier<T> {
    private volatile T value;
    private final Supplier<T> loader;
    private final AtomicBoolean initialized = new AtomicBoolean();
    private final Queue<Consumer<T>> creationConsumers = queue();
    private final List<Consumer<T>> initializationConsumers = linkedList();
    private final List<Consumer<T>> disposeConsumers = linkedList();

    public DisposableProperty<T> initialize() {
        get();
        return this;
    }


    public DisposableProperty<T> created(Consumer<T> consumer) {
        creationConsumers.add(consumer);
        return this;
    }

    public DisposableProperty<T> initialized(Consumer<T> consumer) {
        initializationConsumers.add(consumer);
        return this;
    }

    public DisposableProperty<T> disposed(Consumer<T> consumer) {
        disposeConsumers.add(consumer);
        return this;
    }


    public boolean initialized() {
        return initialized.get();
    }

    public boolean disposed() {
        return !initialized();
    }


    public void dispose() {
        while (nonNull(value)) {
            if (this.initialized.compareAndSet(true, false)) {
                T current = value;
                value = null;
                disposeConsumers.forEach(consumer -> consumer.accept(current));
            }
        }
    }

    @Override
    public T get() {
        while (isNull(value)) {
            if (this.initialized.compareAndSet(false, true)) {
                value = orThrow(loader.get(), new InternalRuntimeException(MANAGED_VALUE_IS_NULL));
                erase(creationConsumers, consumer -> consumer.accept(value));
                initializationConsumers.forEach(consumer -> consumer.accept(value));
            }
        }
        return value;
    }

    public T value() {
        return value;
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
        if (!(other instanceof DisposableProperty)) {
            return false;
        }
        return get().equals(((DisposableProperty<?>) other).get());
    }

    public static <T> DisposableProperty<T> disposable(Supplier<T> factory) {
        return new DisposableProperty<>(factory);
    }

    public static <T> DisposableProperty<T> disposable(Supplier<T> factory, Consumer<T> disposer) {
        return new DisposableProperty<>(factory).disposed(disposer);
    }
}
