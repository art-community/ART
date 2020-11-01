/*
 * ART
 *
 * Copyright 2020 ART
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

package io.art.model.server;

import com.google.common.collect.*;
import io.art.rsocket.configuration.*;
import io.art.server.configuration.*;
import io.art.server.registry.*;
import lombok.*;
import static io.art.model.constants.ModelConstants.Protocols.*;
import java.util.*;
import java.util.function.*;

public class ServerModel {
    private final ImmutableSet.Builder<ServiceModel<?>> services = ImmutableSet.builder();

    public ServerModel rsocket(UnaryOperator<ServiceModel<RsocketServiceConfiguration>> model) {
        services.add(model.apply(new ServiceModel<>(RSOCKET)));
        return this;
    }

    public ImmutableSet<ServiceModel<?>> getServices() {
        return services.build();
    }

}