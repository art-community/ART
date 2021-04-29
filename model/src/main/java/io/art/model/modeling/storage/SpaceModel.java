package io.art.model.modeling.storage;

import io.art.storage.space.Space;

import java.util.function.Function;
import java.util.function.Supplier;

public interface SpaceModel {
    String getId();

    Class<?> getSpaceModelClass();

    Class<?> getPrimaryKeyClass();

    Class<?> getBasicSpaceInterface();

    <C, K, V> Supplier<Space<K, V>> implement(Function<C, ? extends Space<K, V>> generatedSpaceBuilder);
}
