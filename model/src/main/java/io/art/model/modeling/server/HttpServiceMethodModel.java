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

import io.art.server.specification.ServiceMethodSpecification.*;
import lombok.*;

import java.util.function.*;

import static io.art.http.constants.HttpModuleConstants.*;
import static io.art.value.constants.ValueModuleConstants.*;

@Getter
@Builder
public class HttpServiceMethodModel implements ServiceMethodModel {
    private final String id;
    private final String name;
    private final String filePath;
    private final String directoryDefaultFileName;
    private final boolean deactivated;
    private final boolean logging;
    private final HttpMethodType httpMethodType;
    private final Function<ServiceMethodSpecificationBuilder, ServiceMethodSpecificationBuilder> decorator;
    private final DataFormat defaultDataFormat;
}
