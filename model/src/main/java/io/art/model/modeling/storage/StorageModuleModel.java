package io.art.model.modeling.storage;


import io.art.core.collection.*;
import io.art.storage.space.Space;
import lombok.*;
import static io.art.core.collection.ImmutableMap.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;


@RequiredArgsConstructor
@Getter
public class StorageModuleModel {
    private final ImmutableMap<String, TarantoolSpaceModel> tarantoolSpaces;

    public <C, K, V> Supplier<Space<K, V>> implement(String id, Function<C, ? extends Space<K, V>> generatedSpaceBuilder) {
        return getStorages().get(id).implement(generatedSpaceBuilder);
    }

    public ImmutableMap<String, SpaceModel> getStorages() {
        return tarantoolSpaces.entrySet().stream().collect(immutableMapCollector(Map.Entry::getKey, Map.Entry::getValue));
    }

}
