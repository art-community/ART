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

package io.art.rsocket.configuration;

import io.art.core.module.*;
import io.art.core.source.*;
import io.art.rsocket.refresher.*;
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.rsocket.constants.RsocketModuleConstants.ConfigurationKeys.*;
import static java.util.Optional.*;

@RequiredArgsConstructor
public class RsocketModuleConfiguration implements ModuleConfiguration {
    private final RsocketModuleRefresher refresher;

    @Getter(lazy = true)
    private final RsocketModuleRefresher.Consumer consumer = refresher.consumer();

    @Getter
    private RsocketServerConfiguration serverConfiguration;

    @Getter
    private RsocketCommunicatorConfiguration communicatorConfiguration;

    @Getter
    private boolean activateServer;

    @Getter
    private boolean activateCommunicator;

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<RsocketModuleConfiguration, Configurator> {
        private final RsocketModuleConfiguration configuration;

        @Override
        public Configurator from(ConfigurationSource source) {
            ofNullable(source.getNested(RSOCKET_SECTION))
                    .map(rsocket -> rsocket.getNested(SERVER_SECTION))
                    .map(server -> RsocketServerConfiguration.from(configuration.refresher, server))
                    .ifPresent(serverConfiguration -> configuration.serverConfiguration = serverConfiguration);
            ofNullable(source.getNested(RSOCKET_SECTION))
                    .map(rsocket -> rsocket.getNested(COMMUNICATOR_SECTION))
                    .map(communicator -> RsocketCommunicatorConfiguration.from(configuration.refresher, communicator))
                    .ifPresent(communicatorConfiguration -> configuration.communicatorConfiguration = communicatorConfiguration);
            configuration.serverConfiguration = orElse(configuration.serverConfiguration, RsocketServerConfiguration::defaults);

            configuration.refresher.produce();
            return this;
        }

        @Override
        public Configurator configure(RsocketModuleConfiguration configuration) {
            this.configuration.activateCommunicator = configuration.isActivateCommunicator();
            this.configuration.activateServer = configuration.isActivateServer();
            return this;
        }
    }

}
