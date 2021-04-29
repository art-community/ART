package io.art.model.modeling.storage;

import io.art.storage.space.Space;
import io.art.tarantool.space.*;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.art.core.caster.Caster.cast;
import static io.art.tarantool.module.TarantoolModule.tarantoolInstance;

@Getter
@Builder
public class TarantoolSpaceModel implements SpaceModel {
    private final String cluster;
    private final String space;
    private final Class<?> spaceModelClass;
    private final Class<?> primaryKeyClass;
    private final Map<String, Class<?>> searchers;
    private final Function<?, Long> bucketIdGenerator;

    public String getId(){
        return spaceModelClass.getSimpleName();
    }

    @Override
    public Class<?> getBasicSpaceInterface() {
        return TarantoolSpace.class;
    }

    @Override
    public <C, K, V> Supplier<Space<K, V>> implement(Function<C, ? extends Space<K, V>> generatedSpaceBuilder) {
        return () -> {
            TarantoolSpaceImplementation<K,V> space = cast(generatedSpaceBuilder.apply(cast(tarantoolInstance(cluster).getTransactionManager())));
            space.bucketIdGenerator(cast(bucketIdGenerator));
            return space;
        };
    }
}
