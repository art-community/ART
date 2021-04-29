package io.art.model.configurator;

import io.art.model.modeling.storage.*;
import lombok.*;

import java.util.*;
import java.util.function.*;

import static io.art.core.constants.EmptyFunctions.*;
import static io.art.core.factory.MapFactory.*;
import static io.art.tarantool.constants.TarantoolModuleConstants.*;
import static lombok.AccessLevel.*;

@Getter(value = PACKAGE)
public class TarantoolSpaceModelConfigurator<T, K> {
    private String cluster = DEFAULT_TARANTOOL_CLUSTER_NAME;
    private final String space;
    private final Class<T> spaceModelClass;
    private final Class<K> primaryKeyClass;
    private final Map<String, Class<?>> searchers = map();
    private Function<T, Long> bucketIdGenerator = emptyFunction();

    public TarantoolSpaceModelConfigurator(String space, Class<T> spaceModelClass, Class<K> primaryKeyClass){
        this.space = space;
        this.spaceModelClass = spaceModelClass;
        this.primaryKeyClass = primaryKeyClass;
    }

    protected TarantoolSpaceModelConfigurator<T, K> cluster(String cluster){
        this.cluster = cluster;
        return this;
    }

    public TarantoolSpaceModelConfigurator<T, K> sharded(Function<T, Long> bucketIdGenerator){
        this.bucketIdGenerator = bucketIdGenerator;
        return this;
    }

    public TarantoolSpaceModelConfigurator<T, K> searchBy(String indexName, Class<?> keyClass){
        searchers.put(indexName, keyClass);
        return this;
    }

    TarantoolSpaceModel configure() {
        return TarantoolSpaceModel.builder()
                .cluster(cluster)
                .space(space)
                .spaceModelClass(spaceModelClass)
                .primaryKeyClass(primaryKeyClass)
                .searchers(searchers)
                .bucketIdGenerator(bucketIdGenerator)
                .build();
    }
}
