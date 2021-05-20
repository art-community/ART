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

package io.art.server.module;

import io.art.core.annotation.*;
import io.art.core.module.*;
import io.art.server.configuration.*;
import io.art.server.refresher.*;
import io.art.server.registry.*;
import lombok.*;

@UsedByGenerator
public class ServerInitializer implements ModuleInitializer<ServerModuleConfiguration, ServerModuleConfiguration.Configurator, ServerModule> {
    private ServiceSpecificationRegistry registry;

    public ServerInitializer registry(ServiceSpecificationRegistry registry) {
        this.registry = registry;
        return this;
    }

    @Override
    public ServerModuleConfiguration initialize(ServerModule module) {
        Initial initial = new Initial(module.getRefresher());
        initial.registry = registry;
        return initial;
    }

    @Getter
    private static class Initial extends ServerModuleConfiguration {
        private ServiceSpecificationRegistry registry;

        public Initial(ServerModuleRefresher refresher) {
            super(refresher);
        }
    }
}
