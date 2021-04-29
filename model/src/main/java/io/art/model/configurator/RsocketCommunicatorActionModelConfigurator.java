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
import static java.util.function.Function.*;
import static lombok.AccessLevel.*;
import java.util.function.*;

@Getter(value = PACKAGE)
public class RsocketCommunicatorActionModelConfigurator {
    private final String name;
    private String id;
    private String targetServiceId;
    private String targetMethodId;
    private Function<CommunicatorActionBuilder, CommunicatorActionBuilder> decorator = identity();

    public RsocketCommunicatorActionModelConfigurator(String name) {
        this.name = name;
        this.id = name;
    }

    public RsocketCommunicatorActionModelConfigurator id(String id) {
        this.id = id;
        return this;
    }

    public RsocketCommunicatorActionModelConfigurator to(Class<?> targetService) {
        return to(targetService.getSimpleName());
    }

    public RsocketCommunicatorActionModelConfigurator to(Class<?> targetService, String targetMethodId) {
        return to(targetService.getSimpleName(), targetMethodId);
    }

    public RsocketCommunicatorActionModelConfigurator to(String targetServiceId) {
        this.targetServiceId = targetServiceId;
        this.targetMethodId = id;
        return this;
    }

    public RsocketCommunicatorActionModelConfigurator to(String targetServiceId, String targetMethodId) {
        this.targetServiceId = targetServiceId;
        this.targetMethodId = targetMethodId;
        return this;
    }

    public RsocketCommunicatorActionModelConfigurator decorate(Function<CommunicatorActionBuilder, CommunicatorActionBuilder> decorator) {
        this.decorator = decorator.andThen(decorator);
        return this;
    }

    public RsocketCommunicatorActionModelConfigurator implement(Function<RsocketCommunicatorActionBuilder, RsocketCommunicatorActionBuilder> implementor) {
        return decorate(builder -> {
            CommunicatorAction current = builder.build();
            RsocketCommunicatorAction implementation = cast(current.getImplementation());
            return current.toBuilder().implementation(implementor.apply(implementation.toBuilder()).build());
        });
    }

    RsocketCommunicatorActionModel configure() {
        return RsocketCommunicatorActionModel.builder()
                .id(id)
                .name(name)
                .decorator(decorator)
                .targetServiceId(targetServiceId)
                .targetMethodId(targetMethodId)
                .build();
    }
}
