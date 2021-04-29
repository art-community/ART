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

import io.art.communicator.action.*;
import io.art.communicator.action.CommunicatorAction.*;
import io.art.model.modeling.communicator.*;
import io.art.rsocket.communicator.*;
import io.art.rsocket.communicator.RsocketCommunicatorAction.*;
import lombok.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.factory.MapFactory.*;
import static lombok.AccessLevel.*;
import java.util.*;
import java.util.function.*;

@Getter(value = PACKAGE)
public class RsocketCommunicatorModelConfigurator {
    private final Class<?> communicatorInterface;
    private final Map<String, RsocketCommunicatorActionModelConfigurator> actions = map();
    private String id;
    private String targetServiceId;
    private BiFunction<String, CommunicatorActionBuilder, CommunicatorActionBuilder> decorator = (id, builder) -> builder;

    public RsocketCommunicatorModelConfigurator(Class<?> communicatorInterface) {
        this.communicatorInterface = communicatorInterface;
        this.id = communicatorInterface.getSimpleName();
    }

    public RsocketCommunicatorModelConfigurator id(String id) {
        this.id = id;
        return this;
    }

    public RsocketCommunicatorModelConfigurator to(Class<?> targetService) {
        return to(targetService.getSimpleName());
    }

    public RsocketCommunicatorModelConfigurator to(String targetServiceId) {
        this.targetServiceId = targetServiceId;
        return this;
    }

    public RsocketCommunicatorModelConfigurator action(String name) {
        return action(name, UnaryOperator.identity());
    }

    public RsocketCommunicatorModelConfigurator action(String name, UnaryOperator<RsocketCommunicatorActionModelConfigurator> configurator) {
        actions.putIfAbsent(name, configurator.apply(new RsocketCommunicatorActionModelConfigurator(name)));
        return this;
    }

    public RsocketCommunicatorModelConfigurator decorate(BiFunction<String, CommunicatorActionBuilder, CommunicatorActionBuilder> decorator) {
        BiFunction<String, CommunicatorActionBuilder, CommunicatorActionBuilder> current = this.decorator;
        this.decorator = (method, builder) -> {
            builder = current.apply(method, builder);
            return decorator.apply(method, builder);
        };
        return this;
    }

    public RsocketCommunicatorModelConfigurator implement(BiFunction<String, RsocketCommunicatorActionBuilder, RsocketCommunicatorActionBuilder> implementor) {
        return decorate((id, builder) -> {
            CommunicatorAction action = builder.build();
            RsocketCommunicatorAction implementation = cast(action.getImplementation());
            return builder.implementation(implementor.apply(id, implementation.toBuilder()).build());
        });
    }

    RsocketCommunicatorModel configure() {
        return RsocketCommunicatorModel.builder()
                .id(id)
                .communicatorInterface(communicatorInterface)
                .targetServiceId(targetServiceId)
                .decorator(decorator)
                .actions(actions
                        .entrySet()
                        .stream()
                        .collect(immutableMapCollector(entry -> entry.getValue().getId(), entry -> entry.getValue().configure())))
                .build();
    }
}
