/*
 * ART Java
 *
 * Copyright 2019 ART
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

package ru.art.http.server.module;

import lombok.*;
import ru.art.core.extension.*;
import ru.art.core.module.Module;
import ru.art.http.server.*;
import ru.art.http.server.specification.*;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.http.server.HttpServerModuleConfiguration.*;
import static ru.art.http.server.constants.HttpServerModuleConstants.*;
import static ru.art.service.ServiceModule.*;
import java.util.*;

@Getter
public class HttpServerModule implements Module<HttpServerModuleConfiguration, HttpServerModuleState> {
    @Getter(lazy = true, onMethod = @__({@SuppressWarnings("unchecked")}), value = PRIVATE)
    private final static List<HttpServiceSpecification> httpServices = serviceModuleState().getServiceRegistry()
            .getServices()
            .values()
            .stream()
            .filter(service -> service.getServiceTypes().contains(HTTP_SERVICE_TYPE))
            .map(service -> (HttpServiceSpecification) service)
            .collect(toList());
    @Getter(lazy = true, value = PRIVATE)
    private final static HttpServerModuleConfiguration httpServerModule = context().getModule(HTTP_SERVER_MODULE_ID, HttpServerModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private final static HttpServerModuleState httpServerModuleState = context().getModuleState(HTTP_SERVER_MODULE_ID, HttpServerModule::new);
    private final String id = HTTP_SERVER_MODULE_ID;
    private final HttpServerModuleConfiguration defaultConfiguration = DEFAULT_CONFIGURATION;
    private final HttpServerModuleState state = new HttpServerModuleState();

    public static HttpServerModuleConfiguration httpServerModule() {
        if (contextIsNotReady()) {
            return DEFAULT_CONFIGURATION;
        }
        return getHttpServerModule();
    }

    public static List<HttpServiceSpecification> httpServices() {
        return getHttpServices();
    }

    public static HttpServerModuleState httpServerModuleState() {
        return getHttpServerModuleState();
    }

    @Override
    public void onUnload() {
        doIfNotNull(httpServerModuleState().getServer(), HttpServer::stop);
    }
}
