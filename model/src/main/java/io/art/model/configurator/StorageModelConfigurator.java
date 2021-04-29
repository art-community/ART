package io.art.model.configurator;

import io.art.core.collection.ImmutableMap;
import io.art.core.collection.ImmutableSet;
import io.art.model.modeling.storage.StorageModuleModel;
import io.art.model.modeling.storage.TarantoolSpaceModel;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import static io.art.core.collection.ImmutableMap.immutableMapCollector;
import static io.art.core.collection.ImmutableSet.immutableSetBuilder;
import static io.art.core.factory.ArrayFactory.streamOf;
import static java.util.function.Function.identity;

public class StorageModelConfigurator {
    private final ImmutableSet.Builder<TarantoolStorageModelConfigurator> tarantoolClusterConfigurators = immutableSetBuilder();


    public final StorageModelConfigurator tarantool(String clusterId) {
        tarantoolClusterConfigurators.add(new TarantoolStorageModelConfigurator(clusterId));
        return this;
    }

    public final StorageModelConfigurator tarantool() {
        tarantoolClusterConfigurators.add(new TarantoolStorageModelConfigurator());
        return this;
    }

    @SafeVarargs
    public final StorageModelConfigurator tarantool(UnaryOperator<TarantoolStorageModelConfigurator>... configurators) {
        streamOf(configurators)
                .map(configurator -> (Function<TarantoolStorageModelConfigurator, TarantoolStorageModelConfigurator>) configurator)
                .reduce(Function::andThen)
                .map(configurator -> configurator.apply(new TarantoolStorageModelConfigurator()))
                .ifPresent(tarantoolClusterConfigurators::add);
        return this;
    }

    @SafeVarargs
    public final StorageModelConfigurator tarantool(String clusterId, UnaryOperator<TarantoolStorageModelConfigurator>... configurators) {
        streamOf(configurators)
                .map(configurator -> (Function<TarantoolStorageModelConfigurator, TarantoolStorageModelConfigurator>) configurator)
                .reduce(Function::andThen)
                .map(configurator -> configurator.apply(new TarantoolStorageModelConfigurator(clusterId)))
                .ifPresent(tarantoolClusterConfigurators::add);
        return this;
    }

    StorageModuleModel configure() {
        ImmutableMap<String, TarantoolSpaceModel> tarantoolSpaces = this.tarantoolClusterConfigurators.build()
                .stream()
                .flatMap(TarantoolStorageModelConfigurator::configure)
                .collect(immutableMapCollector(TarantoolSpaceModel::getId, identity()));
        return new StorageModuleModel(tarantoolSpaces);
    }
}
