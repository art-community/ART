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

package io.art.communicator.module;

import io.art.communicator.configuration.*;
import io.art.communicator.refresher.*;
import io.art.communicator.registry.*;
import io.art.core.annotation.*;
import io.art.core.module.*;
import lombok.*;

@UsedByGenerator
public class CommunicatorInitializer implements ModuleInitializer<CommunicatorModuleConfiguration, CommunicatorModuleConfiguration.Configurator, CommunicatorModule> {
    private CommunicatorProxyRegistry registry;

    public CommunicatorInitializer registry(CommunicatorProxyRegistry registry) {
        this.registry = registry;
        return this;
    }

    @Override
    public CommunicatorModuleConfiguration initialize(CommunicatorModule module) {
        Initial initial = new Initial(module.getRefresher());
        initial.registry = registry;
        return initial;
    }

    @Getter
    private static class Initial extends CommunicatorModuleConfiguration {
        private CommunicatorProxyRegistry registry;

        public Initial(CommunicatorModuleRefresher refresher) {
            super(refresher);
        }
    }
}
