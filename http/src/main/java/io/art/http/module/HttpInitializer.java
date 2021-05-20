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

package io.art.http.module;

import io.art.core.annotation.*;
import io.art.core.collection.*;
import io.art.core.module.*;
import io.art.http.configuration.*;
import io.art.http.refresher.*;
import lombok.*;

@Getter
@UsedByGenerator
public class HttpInitializer implements ModuleInitializer<HttpModuleConfiguration, HttpModuleConfiguration.Configurator, HttpModule> {
    private HttpServerConfiguration serverConfiguration;
    private boolean activateServer;
    private boolean activateCommunicator;

    public HttpInitializer server(HttpServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
        return this;
    }

//    public HttpInitializer server(HttpServerModel httpServerModel) {
//        HttpServer server = HttpServer.createLogger()
//                .httpRequestDecoder(httpServerModel.getRequestDecoderConfigurator())
//                .wiretap(httpServerModel.isWiretap())
//                .accessLog(httpServerModel.isAccessLogging())
//                .host(httpServerModel.getHost())
//                .port(httpServerModel.getPort())
//                .protocol(httpServerModel.getProtocol())
//                .compress(httpServerModel.isCompression());
//        let(httpServerModel.getSslConfigurator(), configurator -> server.secure(configurator, httpServerModel.isRedirectToHttps()));
//
//        HttpServerConfiguration.HttpServerConfigurationBuilder serverConfigurationBuilder = HttpServerConfiguration.builder()
//                .httpServer(server)
//                .defaultDataFormat(httpServerModel.getDefaultDataFormat())
//                .fragmentationMtu(httpServerModel.getFragmentationMtu())
//                .logging(httpServerModel.isLogging())
//                .services(httpServerModel.getServices()
//                        .values()
//                        .stream()
//                        .collect(cast(immutableMapCollector(HttpServiceModel::getId, this::buildServiceConfig))))
//                .exceptionMapper(httpServerModel.getExceptionsMapper());
//        let(httpServerModel.getDefaultServiceMethod(), serverConfigurationBuilder::defaultServiceMethod);
//
//        server(serverConfigurationBuilder.build());
//        return this;
//    }

    public HttpInitializer services(ImmutableMap<String, HttpServiceConfiguration> services) {
        serverConfiguration = HttpServerConfiguration.defaults().toBuilder().services(services).build();
        return this;
    }

    public HttpInitializer activateServer() {
        activateServer = true;
        return this;
    }

    public HttpInitializer activateCommunicator() {
        activateCommunicator = true;
        return this;
    }
//
//    private HttpServiceConfiguration buildServiceConfig(HttpServiceModel serviceModel) {
//        Map<String, HttpMethodConfiguration> configs = map();
//
//        serviceModel.getHttpMethods()
//                .forEach((id, method) -> configs.put(id,
//                        HttpMethodConfiguration.builder()
//                                .path(serviceModel.getPath().endsWith(SLASH) ?
//                                        serviceModel.getPath() + method.getName() :
//                                        serviceModel.getPath() + SLASH + method.getName()
//                                )
//                                .filePath(method.getFilePath())
//                                .deactivated(method.isDeactivated())
//                                .logging(method.isLogging())
//                                .method(method.getHttpMethodType())
//                                .defaultDataFormat(method.getDefaultDataFormat())
//                                .build()
//                        )
//                );
//
//        ServerModule.serverModule().configuration().getRegistry().getServices()
//                .get(serviceModel.getId())
//                .getMethods().keySet()
//                .forEach(id -> configs.putIfAbsent(id,
//                        HttpMethodConfiguration.builder()
//                                .path(serviceModel.getPath().endsWith(SLASH) ?
//                                        serviceModel.getPath() + id :
//                                        serviceModel.getPath() + SLASH + id
//                                )
//                                .deactivated(false)
//                                .logging(serviceModel.isLogging())
//                                .method(GET)
//                                .defaultDataFormat(serviceModel.getDefaultDataFormat())
//                                .build()
//                        )
//                );
//
//        return HttpServiceConfiguration.builder()
//                .path(serviceModel.getPath())
//                .methods(immutableMapOf(configs))
//                .build();
//    }

    @Override
    public HttpModuleConfiguration initialize(HttpModule module) {
        Initial initial = new Initial(module.getRefresher());
        initial.serverConfiguration = serverConfiguration;
        initial.activateCommunicator = activateCommunicator;
        initial.activateServer = activateServer;
        return initial;
    }

    @Getter
    public static class Initial extends HttpModuleConfiguration {
        private HttpServerConfiguration serverConfiguration;
        private boolean activateServer;
        private boolean activateCommunicator;

        public Initial(HttpModuleRefresher refresher) {
            super(refresher);
        }
    }
}
