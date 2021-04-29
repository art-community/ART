/*
 * ART
 *
 * Copyright 2019-2021 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.art.model.configurator;

import io.art.core.collection.*;
import io.art.core.operator.*;
import io.art.model.modeling.server.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.collection.ImmutableSet.*;
import static io.art.core.factory.ArrayFactory.*;
import static java.util.function.UnaryOperator.*;

import java.util.function.*;

public class ServerModelConfigurator {
    private final ImmutableSet.Builder<RsocketServiceModelConfigurator> rsocketServices = immutableSetBuilder();
    private final HttpServerModelConfigurator httpServerConfigurator = new HttpServerModelConfigurator();

    public final ServerModelConfigurator rsocket(Class<?> service) {
        rsocketServices.add(new RsocketServiceModelConfigurator(service));
        return this;
    }

    @SafeVarargs
    public final ServerModelConfigurator rsocket(Class<?> service, UnaryOperator<RsocketServiceModelConfigurator>... configurators) {
        streamOf(configurators)
                .reduce(Operators::andThen)
                .map(configurator -> configurator.apply(new RsocketServiceModelConfigurator(service)))
                .ifPresent(rsocketServices::add);
        return this;
    }

    @SafeVarargs
    public final ServerModelConfigurator http(UnaryOperator<HttpServerModelConfigurator>... configurators) {
        streamOf(configurators).forEach(configurator -> configurator.apply(httpServerConfigurator));
        return this;
    }

    ServerModuleModel configure() {
        ImmutableMap<String, RsocketServiceModel> rsocketServices = this.rsocketServices.build()
                .stream()
                .map(RsocketServiceModelConfigurator::configure)
                .collect(immutableMapCollector(RsocketServiceModel::getId, identity()));
        HttpServerModel httpServer = httpServerConfigurator.configure();
        return ServerModuleModel.builder()
                .rsocketServices(rsocketServices)
                .httpServer(httpServer)
                .httpServices(httpServer.getServices())
                .build();
    }
}
