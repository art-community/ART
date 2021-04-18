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

package io.art.model.customizer;

import io.art.core.annotation.*;
import io.art.server.configuration.*;
import io.art.server.module.*;
import io.art.server.refresher.*;
import io.art.server.registry.*;
import lombok.*;

@Getter
@UsedByGenerator
public class ServerCustomizer {
    private final Custom configuration;

    public ServerCustomizer(ServerModule module) {
        this.configuration = new ServerCustomizer.Custom(module.getRefresher());
    }

    public ServerCustomizer registry(ServiceSpecificationRegistry registry) {
        configuration.registry = registry;
        return this;
    }

    @Getter
    private static class Custom extends ServerModuleConfiguration {
        private ServiceSpecificationRegistry registry;

        public Custom(ServerModuleRefresher refresher) {
            super(refresher);
        }
    }
}
