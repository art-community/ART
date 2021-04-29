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
import io.art.server.specification.ServiceMethodSpecification.*;
import lombok.*;

import java.util.function.*;

import static io.art.core.caster.Caster.*;
import static io.art.value.constants.ValueModuleConstants.*;

@Getter
@Builder
public class HttpServiceModel implements ServiceModel {
    private final String id;
    private final String path;
    private final boolean logging;
    private final Class<?> serviceClass;
    private final BiFunction<String, ServiceMethodSpecificationBuilder, ServiceMethodSpecificationBuilder> decorator;
    private final ImmutableMap<String, HttpServiceMethodModel> methods;
    private final DataFormat defaultDataFormat;

    public ImmutableMap<String, ServiceMethodModel> getMethods() {
        return cast(methods);
    }

    public ImmutableMap<String, HttpServiceMethodModel> getHttpMethods() {
        return methods;
    }
}
