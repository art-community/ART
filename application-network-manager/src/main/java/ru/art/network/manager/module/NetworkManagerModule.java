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

package ru.art.network.manager.module;

import lombok.*;
import ru.art.core.module.Module;
import ru.art.network.manager.client.*;
import ru.art.network.manager.configuration.*;
import ru.art.network.manager.refresher.*;
import ru.art.network.manager.state.*;
import ru.art.state.api.communication.grpc.*;
import static java.time.Duration.*;
import static java.time.temporal.ChronoUnit.*;
import static lombok.AccessLevel.PRIVATE;
import static ru.art.core.context.Context.*;
import static ru.art.network.manager.configuration.NetworkManagerModuleConfiguration.*;
import static ru.art.network.manager.constants.NetworkManagerModuleConstants.*;
import static ru.art.service.ServiceModule.*;
import static ru.art.task.deferred.executor.SchedulerModuleActions.*;
import static ru.art.task.deferred.executor.TaskFactory.*;

@Getter
public class NetworkManagerModule implements Module<NetworkManagerModuleConfiguration, NetworkManagerModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private static final NetworkManagerModuleConfiguration networkManagerModule = context().getModule(NETWORK_MANAGER_MODULE_ID, NetworkManagerModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private static final NetworkManagerModuleState networkManagerModuleState = context().getModuleState(NETWORK_MANAGER_MODULE_ID, NetworkManagerModule::new);
    private final String id = NETWORK_MANAGER_MODULE_ID;
    private final NetworkManagerModuleConfiguration defaultConfiguration = NetworkManagerModuleDefaultConfiguration.DEFAULT_CONFIGURATION;
    private final NetworkManagerModuleState state = new NetworkManagerModuleState();

    public static NetworkManagerModuleConfiguration networkManagerModule() {
        if (contextIsNotReady()) {
            return DEFAULT_CONFIGURATION;
        }
        return getNetworkManagerModule();
    }

    public static NetworkManagerModuleState networkManagerModuleState() {
        return getNetworkManagerModuleState();
    }

    @Override
    public void onLoad() {
        asynchronousPeriod(commonRunnableTask(ModuleEndpointsRefresher::refreshModuleEndpoints), of(networkManagerModule().getRefreshRateSeconds(), SECONDS));
        asynchronousPeriod(commonRunnableTask(ApplicationStateClient::connect), of(networkManagerModule().getConnectionPingRateSeconds(), SECONDS));
        int statePort = networkManagerModule().getStatePort();
        String stateHost = networkManagerModule().getStateHost();
        String statePath = networkManagerModule().getStatePath();
        serviceModuleState().getServiceRegistry()
                .registerService(new NetworkServiceProxySpecification(statePath, stateHost, statePort))
                .registerService(new LockServiceProxySpecification(statePath, stateHost, statePort));
    }
}
