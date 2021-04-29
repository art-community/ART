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

import java.util.function.*;

import static io.art.http.constants.HttpModuleConstants.*;
import static io.art.value.constants.ValueModuleConstants.*;
import static java.util.function.Function.*;
import static lombok.AccessLevel.*;

@Getter(value = PACKAGE)
public class HttpServiceMethodModelConfigurator {
    private final String id;
    private String path;
    private String filePath;
    private String directoryDefaultFileName;
    private boolean deactivated = false;
    private boolean logging = false;
    private HttpMethodType httpMethodType = HttpMethodType.GET;
    private Function<ServiceMethodSpecificationBuilder, ServiceMethodSpecificationBuilder> decorator = identity();
    private DataFormat defaultDataFormat;

    public HttpServiceMethodModelConfigurator(String methodName) {
        this.path = methodName;
        this.id = methodName;
    }

    public HttpServiceMethodModelConfigurator path(String path) {
        this.path = path;
        return this;
    }

    public HttpServiceMethodModelConfigurator deactivate() {
        deactivated = false;
        return this;
    }

    public HttpServiceMethodModelConfigurator logging(boolean isLogging) {
        logging = isLogging;
        return this;
    }

    protected HttpServiceMethodModelConfigurator httpMethod(HttpMethodType httpMethodType) {
        this.httpMethodType = httpMethodType;
        return this;
    }

    protected HttpServiceMethodModelConfigurator filePath(String path) {
        this.filePath = path;
        return this;
    }

    protected HttpServiceMethodModelConfigurator directoryDefaultFileName(String path) {
        this.directoryDefaultFileName = path;
        return this;
    }

    public HttpServiceMethodModelConfigurator defaultDataFormat(DataFormat format) {
        defaultDataFormat = format;
        return this;
    }

    public HttpServiceMethodModelConfigurator decorate(Function<ServiceMethodSpecificationBuilder, ServiceMethodSpecificationBuilder> decorator) {
        this.decorator = decorator.andThen(decorator);
        return this;
    }

    HttpServiceMethodModel configure() {
        return HttpServiceMethodModel.builder()
                .id(id)
                .name(path)
                .filePath(filePath)
                .directoryDefaultFileName(directoryDefaultFileName)
                .deactivated(deactivated)
                .logging(logging)
                .httpMethodType(httpMethodType)
                .decorator(decorator)
                .defaultDataFormat(defaultDataFormat)
                .build();
    }
}
