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

package ru.art.remote.scheduler.module;

import lombok.*;
import ru.art.core.module.Module;
import ru.art.remote.scheduler.api.communicator.grpc.RemoteSchedulerServiceGrpcCommunicationSpec;
import ru.art.remote.scheduler.configuration.*;
import ru.art.remote.scheduler.state.*;
import static lombok.AccessLevel.*;
import static ru.art.core.context.Context.*;
import static ru.art.remote.scheduler.constants.RemoteSchedulerModuleConstants.*;
import static ru.art.remote.scheduler.controller.PoolController.*;
import static ru.art.service.ServiceModule.serviceModuleState;

@Getter
public class RemoteSchedulerModule implements Module<RemoteSchedulerModuleConfiguration, RemoteSchedulerModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private static final RemoteSchedulerModuleConfiguration remoteModule = context().getModule(REMOTE_SCHEDULER_MODULE_ID, RemoteSchedulerModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private static final RemoteSchedulerModuleState remoteModuleState = context().getModuleState(REMOTE_SCHEDULER_MODULE_ID, RemoteSchedulerModule::new);
    private final String id = REMOTE_SCHEDULER_MODULE_ID;
    private final RemoteSchedulerModuleConfiguration defaultConfiguration = new RemoteSchedulerModuleConfiguration();
    private RemoteSchedulerModuleState state = new RemoteSchedulerModuleState();

    public static RemoteSchedulerModuleConfiguration remoteSchedulerModule() {
        return getRemoteModule();
    }

    public static RemoteSchedulerModuleState remoteSchedulerModuleState() {
        return getRemoteModuleState();
    }

    public static void startRemoteScheduler() {
        fillAllPools();
        startPoolRefreshingTask();
    }

    @Override
    public void onLoad() {
        serviceModuleState().getServiceRegistry()
                .registerService(new RemoteSchedulerServiceGrpcCommunicationSpec());
        startRemoteScheduler();
    }
}
