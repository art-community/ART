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

package ru.art.configurator.module;

import lombok.*;
import ru.art.config.module.*;
import ru.art.configurator.configuration.*;
import ru.art.configurator.specification.*;
import ru.art.core.configurator.*;
import ru.art.core.module.Module;
import ru.art.core.module.*;
import ru.art.grpc.client.module.*;
import ru.art.grpc.server.configuration.*;
import ru.art.grpc.server.module.*;
import ru.art.grpc.server.state.*;
import ru.art.http.client.configuration.*;
import ru.art.http.client.module.*;
import ru.art.http.client.state.*;
import ru.art.http.server.*;
import ru.art.http.server.module.*;
import ru.art.http.server.specification.*;
import ru.art.json.module.*;
import ru.art.logging.*;
import ru.art.metrics.configuration.*;
import ru.art.metrics.http.specification.*;
import ru.art.metrics.module.*;
import ru.art.rocks.db.configuration.*;
import ru.art.rocks.db.module.*;
import ru.art.rocks.db.state.*;
import ru.art.service.*;
import static java.util.UUID.*;
import static lombok.AccessLevel.*;
import static ru.art.config.ConfigProvider.*;
import static ru.art.configurator.api.constants.ConfiguratorServiceConstants.*;
import static ru.art.configurator.constants.ConfiguratorModuleConstants.*;
import static ru.art.configurator.constants.ConfiguratorModuleConstants.ConfiguratorLocalConfigKeys.*;
import static ru.art.configurator.service.UserService.*;
import static ru.art.core.configuration.ContextInitialConfiguration.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.ExceptionExtensions.*;
import static ru.art.grpc.server.GrpcServer.*;
import static ru.art.http.constants.HttpCommonConstants.*;
import static ru.art.http.server.HttpServer.*;
import static ru.art.information.specification.InformationServiceSpecification.*;
import static ru.art.service.ServiceModule.*;

@Getter
public class ConfiguratorModule implements Module<ConfiguratorModuleConfiguration, ModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private static final ConfiguratorModuleConfiguration configuratorModule = context().getModule(CONFIGURATOR_MODULE_ID, ConfiguratorModule::new);
    private final String id = CONFIGURATOR_MODULE_ID;
    private final ConfiguratorModuleConfiguration defaultConfiguration = new ConfiguratorModuleConfiguration();

    public static void startConfigurator() {
        ApplicationContextConfiguration configuration = new ApplicationContextConfiguration(CONFIGURATOR_MODULE_ID);
        initContext(configuration)
                .loadModule(new ConfigModule())
                .loadModule(new JsonModule())
                .loadModule(new LoggingModule())
                .loadModule(new ServiceModule())
                .loadModule(new RocksDbModule(), (ModuleConfigurator<RocksDbModuleConfiguration, RocksDbModuleState>) module ->
                        new ConfiguratorRocksDbConfiguration())
                .loadModule(new HttpServerModule(), (ModuleConfigurator<HttpServerModuleConfiguration, HttpServerModuleState>) (module) ->
                        new ConfiguratorHttpServerConfiguration())
                .loadModule(new GrpcServerModule(), (ModuleConfigurator<GrpcServerModuleConfiguration, GrpcServerModuleState>) (module) ->
                        new ConfiguratorGrpcServerConfiguration())
                .loadModule(new MetricsModule(), (ModuleConfigurator<MetricModuleConfiguration, ModuleState>) (module) ->
                        new ConfiguratorMetricsConfiguration())
                .loadModule(new GrpcClientModule())
                .loadModule(new HttpClientModule(), (ModuleConfigurator<HttpClientModuleConfiguration, HttpClientModuleState>) module -> new ConfiguratorHttpClientConfiguration())
                .loadModule(new ConfiguratorModule());
        serviceModuleState()
                .getServiceRegistry()
                .registerService(new ConfiguratorServiceSpecification())
                .registerService(new HttpResourceServiceSpecification(CONFIGURATOR_PATH + WEB_UI_PATH))
                .registerService(new UserServiceSpecification())
                .registerService(new MetricServiceSpecification(CONFIGURATOR_PATH));
        registerInformationService(CONFIGURATOR_PATH);
        startHttpServer();
        startGrpcServer().await();
    }

    public static ConfiguratorModuleConfiguration configuratorModule() {
        return getConfiguratorModule();
    }

    public static void main(String[] args) {
        startConfigurator();
    }

    @Override
    public void onLoad() {
        String userName = ifExceptionOrEmpty(() -> config(CONFIGURATOR_SECTION_ID).getString(CONFIGURATOR_USERNAME), randomUUID().toString());
        String password = ifExceptionOrEmpty(() -> config(CONFIGURATOR_SECTION_ID).getString(CONFIGURATOR_PASSWORD), randomUUID().toString());
        register(userName, password);
    }
}
