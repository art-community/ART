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

import io.art.model.modeling.server.*;
import io.art.server.specification.ServiceMethodSpecification.*;
import lombok.*;
import static java.util.function.Function.*;
import static lombok.AccessLevel.*;
import java.util.function.*;

@Getter(value = PACKAGE)
public class RsocketServiceMethodModelConfigurator {
    private final String name;
    private String id;
    private Function<ServiceMethodSpecificationBuilder, ServiceMethodSpecificationBuilder> decorator = identity();

    public RsocketServiceMethodModelConfigurator(String name) {
        this.name = name;
        this.id = name;
    }

    public RsocketServiceMethodModelConfigurator id(String id) {
        this.id = id;
        return this;
    }

    public RsocketServiceMethodModelConfigurator decorate(Function<ServiceMethodSpecificationBuilder, ServiceMethodSpecificationBuilder> decorator) {
        this.decorator = decorator.andThen(decorator);
        return this;
    }

    RsocketServiceMethodModel configure() {
        return new RsocketServiceMethodModel(id, name, decorator);
    }
}
