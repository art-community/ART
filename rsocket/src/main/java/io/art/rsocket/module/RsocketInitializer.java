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

package io.art.rsocket.module;

import io.art.core.annotation.*;
import io.art.core.module.*;
import io.art.rsocket.configuration.*;
import io.art.rsocket.refresher.*;
import lombok.*;

@UsedByGenerator
public class RsocketInitializer implements ModuleInitializer<RsocketModuleConfiguration, RsocketModuleConfiguration.Configurator, RsocketModule> {
    private boolean activateServer;
    private boolean activateCommunicator;
    private boolean serverLogging;

    public RsocketInitializer activateServer() {
        activateServer = true;
        return this;
    }

    public RsocketInitializer serverLogging(boolean logging) {
        this.serverLogging = logging;
        return this;
    }

    public RsocketInitializer activateCommunicator() {
        activateCommunicator = true;
        return this;
    }

    @Override
    public RsocketModuleConfiguration initialize(RsocketModule module) {
        Initial initial = new Initial(module.getRefresher());
        initial.activateCommunicator = activateCommunicator;
        initial.activateServer = activateServer;
        initial.serverConfiguration = initial.serverConfiguration.toBuilder()
                .logging(serverLogging)
                .build();
        return initial;
    }

    @Getter
    public static class Initial extends RsocketModuleConfiguration {
        private boolean activateServer;
        private boolean activateCommunicator;
        private RsocketServerConfiguration serverConfiguration = super.getServerConfiguration();

        public Initial(RsocketModuleRefresher refresher) {
            super(refresher);
        }
    }
}
