package io.art.core.property;

import io.art.core.changes.*;
import static io.art.core.constants.EmptyFunctions.*;
import static io.art.core.factory.ListFactory.*;
import static io.art.core.factory.QueueFactory.*;
import static io.art.core.property.LazyProperty.*;
import static java.util.Arrays.*;
import static java.util.Objects.*;
import java.util.*;
import java.util.function.*;

public class Property<T> implements Supplier<T> {
    private volatile DisposableProperty<T> disposable;
    private final Consumer<T> disposer;
    private final List<Predicate<T>> predicates = linkedList();
    private final List<Consumer<T>> changeConsumers = linkedList();
    private final List<Consumer<T>> disposeConsumers = linkedList();
    private final Queue<Consumer<T>> creationConsumers = queue();
    private final List<Consumer<T>> initializationConsumers = linkedList();

    public Property(Supplier<T> loader, Consumer<T> disposer) {
        disposable = DisposableProperty.disposable(loader, disposer);
        this.disposer = disposer;
    }


    public LazyProperty<T> immutable() {
        return lazy(disposable);
    }

    public DisposableProperty<T> disposable() {
        return DisposableProperty.disposable(disposable);
    }


    public void initialize() {
        get();
    }

    public boolean initialized() {
        return disposable.initialized();
    }

    public boolean disposed() {
        return disposable.disposed();
    }

    public void dispose() {
        disposable.dispose();
    }


    public Property<T> created(Consumer<T> consumer) {
        creationConsumers.add(consumer);
        disposable.created(consumer);
        return this;
    }

    public Property<T> initialized(Consumer<T> consumer) {
        initializationConsumers.add(consumer);
        disposable.initialized(consumer);
        return this;
    }

    public Property<T> disposed(Consumer<T> consumer) {
        disposeConsumers.add(consumer);
        disposable.disposed(consumer);
        return this;
    }

    public Property<T> changed(Consumer<T> consumer) {
        changeConsumers.add(consumer);
        return this;
    }

    public Property<T> prevent(Predicate<T> predicate) {
        predicates.add(predicate);
        return this;
    }

    public Property<T> listenProperties(Property<?>... others) {
        stream(others).forEach(other -> other.disposed(value -> dispose()).changed(value -> refresh()));
        return this;
    }

    public Property<T> listenConsumer(Supplier<ChangesConsumer> consumer) {
        return created(ignore -> consumer.get().consume(this));
    }

    public Property<T> listen(Supplier<ChangesListener> listener) {
        return created(ignore -> listener.get().consume(this));
    }


    public Property<T> set(T newValue) {
        if (predicates.stream().anyMatch(predicate -> predicate.test(newValue))) {
            return this;
        }
        if (Objects.equals(disposable.get(), newValue)) {
            return this;
        }
        disposable = DisposableProperty.disposable(() -> newValue, disposer);
        creationConsumers.forEach(disposable::created);
        initializationConsumers.forEach(disposable::initialized);
        disposeConsumers.forEach(disposable::disposed);
        changeConsumers.forEach(consumer -> consumer.accept(newValue));
        return this;
    }

    public void refresh() {
        dispose();
        T value = get();
        changeConsumers.forEach(consumer -> consumer.accept(value));
    }


    @Override
    public T get() {
        return disposable.get();
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
        if (!(other instanceof Property)) {
            return false;
        }
        return get().equals(((Property<?>) other).get());
    }


    public static <T> Property<T> property(Supplier<T> loader) {
        return new Property<>(loader, emptyConsumer());
    }

    public static <T> Property<T> property(Supplier<T> loader, Consumer<T> disposer) {
        return new Property<>(loader, disposer);
    }
}
