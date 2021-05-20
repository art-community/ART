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

package io.art.http.configuration;

import io.art.core.module.*;
import io.art.core.source.*;
import io.art.http.refresher.*;
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.http.constants.HttpModuleConstants.ConfigurationKeys.*;
import static java.util.Optional.*;

@RequiredArgsConstructor
public class HttpModuleConfiguration implements ModuleConfiguration {
    private final HttpModuleRefresher refresher;

    @Getter(lazy = true)
    private final HttpModuleRefresher.Consumer consumer = refresher.consumer();

    @Getter
    private HttpServerConfiguration serverConfiguration;

    @Getter
    private HttpCommunicatorConfiguration communicatorConfiguration;

    @Getter
    private boolean activateServer;

    @Getter
    private boolean activateCommunicator;

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<HttpModuleConfiguration, Configurator> {
        private final HttpModuleConfiguration configuration;

        @Override
        public Configurator from(ConfigurationSource source) {
            ofNullable(source.getNested(HTTP_SECTION))
                    .map(http -> http.getNested(SERVER_SECTION))
                    .map(server -> HttpServerConfiguration.from(configuration.refresher, server))
                    .ifPresent(serverConfiguration -> configuration.serverConfiguration = serverConfiguration);
            ofNullable(source.getNested(HTTP_SECTION))
                    .map(http -> http.getNested(COMMUNICATOR_SECTION))
                    .map(communicator -> HttpCommunicatorConfiguration.from(configuration.refresher, communicator))
                    .ifPresent(communicatorConfiguration -> configuration.communicatorConfiguration = communicatorConfiguration);
            configuration.serverConfiguration = orElse(configuration.serverConfiguration, HttpServerConfiguration::defaults);

            configuration.refresher.produce();
            return this;
        }

        @Override
        public Configurator initialize(HttpModuleConfiguration configuration) {
            this.configuration.serverConfiguration = configuration.getServerConfiguration();
            this.configuration.activateCommunicator = configuration.isActivateCommunicator();
            this.configuration.activateServer = configuration.isActivateServer();
            return this;
        }
    }

}
