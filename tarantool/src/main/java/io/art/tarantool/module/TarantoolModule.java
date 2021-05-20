package io.art.tarantool.module;

import io.art.core.module.*;
import io.art.tarantool.configuration.*;
import io.art.tarantool.instance.*;
import io.art.tarantool.module.connection.client.*;
import io.art.tarantool.module.refresher.*;
import io.art.tarantool.module.state.*;
import lombok.*;
import static io.art.core.context.Context.*;
import static io.art.tarantool.configuration.TarantoolModuleConfiguration.*;
import static io.art.tarantool.constants.TarantoolModuleConstants.*;
import static lombok.AccessLevel.*;
import java.util.function.*;

@Getter
public class TarantoolModule implements StatefulModule<TarantoolModuleConfiguration, Configurator, TarantoolModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private static final StatefulModuleProxy<TarantoolModuleConfiguration, TarantoolModuleState> tarantoolModule = context().getStatefulModule(TarantoolModule.class.getSimpleName());
    private final String id = TarantoolModule.class.getSimpleName();
    private final TarantoolModuleRefresher refresher = new TarantoolModuleRefresher();
    private final TarantoolModuleConfiguration configuration = new TarantoolModuleConfiguration(refresher);
    private final Configurator configurator = new Configurator(configuration);
    private final TarantoolModuleState state = new TarantoolModuleState(refresher.consumer());

    public static StatefulModuleProxy<TarantoolModuleConfiguration, TarantoolModuleState> tarantoolModule() {
        return getTarantoolModule();
    }

    public static TarantoolInstance tarantoolInstance() {
        return tarantoolInstance(DEFAULT_TARANTOOL_CLUSTER_NAME);
    }

    public static TarantoolInstance tarantoolInstance(String clusterId) {
        return new TarantoolInstance(getCluster(clusterId));
    }

    private static Supplier<TarantoolCluster> getCluster(String clusterId) {
        return tarantoolModule().state().getCluster(clusterId);
    }
}
