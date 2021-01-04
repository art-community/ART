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

package ru.art.state.module;

import lombok.*;
import ru.art.core.module.Module;
import ru.art.http.server.specification.*;
import ru.art.metrics.http.specification.*;
import ru.art.state.*;
import ru.art.state.configuration.*;
import ru.art.state.configuration.ApplicationStateModuleConfiguration.*;
import ru.art.state.service.*;
import ru.art.state.specification.*;
import static java.time.Duration.*;
import static lombok.AccessLevel.PRIVATE;
import static ru.art.config.extensions.activator.AgileConfigurationsActivator.*;
import static ru.art.core.context.Context.*;
import static ru.art.grpc.server.GrpcServer.*;
import static ru.art.http.server.HttpServer.*;
import static ru.art.service.ServiceModule.*;
import static ru.art.state.api.constants.StateApiConstants.NetworkServiceConstants.Paths.*;
import static ru.art.state.configuration.ApplicationStateModuleConfiguration.*;
import static ru.art.state.constants.StateModuleConstants.*;
import static ru.art.state.dao.ClusterDao.*;
import static ru.art.task.deferred.executor.SchedulerModuleActions.*;
import static ru.art.task.deferred.executor.TaskFactory.*;

@Getter
public class ApplicationStateModule implements Module<ApplicationStateModuleConfiguration, ApplicationState> {
    @Getter(lazy = true, value = PRIVATE)
    private final static ApplicationStateModuleConfiguration applicationStateModule = context().getModule(APPLICATION_STATE_MODULE_ID, ApplicationStateModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private final static ApplicationState applicationState = context().getModuleState(APPLICATION_STATE_MODULE_ID, ApplicationStateModule::new);
    private final String id = APPLICATION_STATE_MODULE_ID;
    private final ApplicationStateModuleConfiguration defaultConfiguration = new ApplicationStateModuleDefaultConfiguration();
    private final ApplicationState state = new ApplicationState();

    public static ApplicationStateModuleConfiguration applicationStateModule() {
        if (contextIsNotReady()) {
            return DEFAULT_CONFIGURATION;
        }
        return getApplicationStateModule();
    }

    public static ApplicationState applicationState() {
        return getApplicationState();
    }

    public static void main(String[] args) {
        startApplicationState();
    }

    @SuppressWarnings("WeakerAccess")
    public static void startApplicationState() {
        useAgileConfigurations(APPLICATION_STATE_MODULE_ID);
        serviceModuleState().getServiceRegistry()
                .registerService(new MetricServiceSpecification(STATE_PATH))
                .registerService(new HttpResourceServiceSpecification(STATE_PATH))
                .registerService(new NetworkServiceSpecification())
                .registerService(new LockServiceSpecification());
        applicationState().setCluster(loadCluster());
        asynchronousPeriod(commonRunnableTask(NetworkService::removeDeadEndpoints), ofSeconds(applicationStateModule().getModuleEndpointCheckRateSeconds()));
        startHttpServer();
        startGrpcServer().await();
    }
}
