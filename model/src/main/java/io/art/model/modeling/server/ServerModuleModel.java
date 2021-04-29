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

package io.art.model.modeling.server;

import io.art.core.collection.*;
import lombok.Builder;
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.collector.MapCollector.*;
import static io.art.core.factory.MapFactory.*;
import static io.art.server.specification.ServiceMethodSpecification.*;
import java.util.*;
import java.util.Map.*;

@Getter
@Builder
public class ServerModuleModel {
    @Builder.Default
    private final ImmutableMap<String, RsocketServiceModel> rsocketServices = emptyImmutableMap();

    @Builder.Default
    private final ImmutableMap<String, HttpServiceModel> httpServices = emptyImmutableMap();

    private final HttpServerModel httpServer;

    public ServiceMethodSpecificationBuilder implement(String serviceId, String methodId, ServiceMethodSpecificationBuilder current) {
        return let(getServices().get(serviceId), service -> service.implement(methodId, current), current);
    }

    public ImmutableMap<String, ServiceModel> getServices() {
        Map<String, ServiceModel> services = rsocketServices.entrySet().stream().collect(mapCollector(Entry::getKey, Entry::getValue));
        services.putAll(httpServices.entrySet().stream().collect(mapCollector(Entry::getKey, Entry::getValue)));
        return immutableMapOf(services);
    }
}
