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

package io.art.model.customizer;

import io.art.core.annotation.*;
import io.art.core.collection.*;
import io.art.http.configuration.*;
import io.art.http.module.*;
import io.art.http.refresher.*;
import io.art.model.implementation.server.*;
import io.art.server.module.*;
import lombok.*;
import reactor.netty.http.server.*;

import java.util.*;

import static io.art.core.caster.Caster.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.factory.MapFactory.*;
import static io.art.http.constants.HttpModuleConstants.HttpMethodType.*;

@Getter
@UsedByGenerator
public class HttpCustomizer {
    private final Custom configuration;

    public HttpCustomizer(HttpModule module) {
        this.configuration = new Custom(module.getRefresher());
    }

    public HttpCustomizer server(HttpServerConfiguration serverConfiguration) {
        configuration.serverConfiguration = serverConfiguration;
        return this;
    }

    public HttpCustomizer server(HttpServerModel httpServerModel) {
        HttpServer server = HttpServer.create()
                .httpRequestDecoder(httpServerModel.getRequestDecoderConfigurator())
                .wiretap(httpServerModel.isWiretap())
                .accessLog(httpServerModel.isAccessLogging())
                .host(httpServerModel.getHost())
                .port(httpServerModel.getPort())
                .protocol(httpServerModel.getProtocol())
                .compress(httpServerModel.isCompression());
        let(httpServerModel.getSslConfigurator(), configurator -> server.secure(configurator, httpServerModel.isRedirectToHttps()));

        HttpServerConfiguration.HttpServerConfigurationBuilder serverConfigurationBuilder = HttpServerConfiguration.builder()
                .httpServer(server)
                .defaultDataFormat(httpServerModel.getDefaultDataFormat())
                .fragmentationMtu(httpServerModel.getFragmentationMtu())
                .logging(httpServerModel.isLogging())
                .services(httpServerModel.getServices()
                        .values()
                        .stream()
                        .collect(cast(immutableMapCollector(HttpServiceModel::getId, this::buildServiceConfig))))
                .exceptionMapper(httpServerModel.getExceptionsMapper());

        server(serverConfigurationBuilder.build());
        return this;
    }

    public HttpCustomizer services(ImmutableMap<String, HttpServiceConfiguration> services) {
        configuration.serverConfiguration = HttpServerConfiguration.defaults().toBuilder().services(services).build();
        return this;
    }

    public HttpCustomizer activateServer() {
        configuration.activateServer = true;
        return this;
    }

    public HttpCustomizer activateCommunicator() {
        configuration.activateCommunicator = true;
        return this;
    }

    private HttpServiceConfiguration buildServiceConfig(HttpServiceModel serviceModel) {
        Map<String, HttpMethodConfiguration> configs = map();

        serviceModel.getHttpMethods()
                .forEach((id, method) -> configs.put(id,
                        HttpMethodConfiguration.builder()
                                .path(serviceModel.getPath() + method.getName())
                                .filePath(method.getFilePath())
                                .directoryDefaultFileName(method.getDirectoryDefaultFileName())
                                .deactivated(method.isDeactivated())
                                .logging(method.isLogging())
                                .method(method.getHttpMethodType())
                                .defaultDataFormat(method.getDefaultDataFormat())
                                .build()
                        )
                );

        ServerModule.serverModule().configuration().getRegistry().getServices()
                .get(serviceModel.getId())
                .getMethods().keySet()
                .forEach(id -> configs.putIfAbsent(id,
                        HttpMethodConfiguration.builder()
                                .path(serviceModel.getPath().endsWith(SLASH) ?
                                        serviceModel.getPath() + id :
                                        serviceModel.getPath() + SLASH + id
                                )
                                .deactivated(false)
                                .logging(serviceModel.isLogging())
                                .method(GET)
                                .defaultDataFormat(serviceModel.getDefaultDataFormat())
                                .build()
                        )
                );

        return HttpServiceConfiguration.builder()
                .path(serviceModel.getPath())
                .methods(immutableMapOf(configs))
                .build();
    }

    @Getter
    public static class Custom extends HttpModuleConfiguration {
        private HttpServerConfiguration serverConfiguration;
        private boolean activateServer;
        private boolean activateCommunicator;

        public Custom(HttpModuleRefresher refresher) {
            super(refresher);
        }
    }
}
