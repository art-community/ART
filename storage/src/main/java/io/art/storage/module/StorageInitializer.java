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

package io.art.storage.module;


import io.art.core.annotation.*;
import io.art.core.module.*;
import io.art.storage.configuration.*;
import io.art.storage.registry.*;
import lombok.*;

@UsedByGenerator
public class StorageInitializer implements ModuleInitializer<StorageModuleConfiguration, StorageModuleConfiguration.Configurator, StorageModule> {
    private final Initial configuration = new Initial();

    public StorageInitializer registry(StorageSpacesRegistry registry) {
        configuration.spacesRegistry = registry;
        return this;
    }

    @Override
    public StorageModuleConfiguration initialize(StorageModule module) {
        return configuration;
    }

    @Getter
    private static class Initial extends StorageModuleConfiguration {
        private StorageSpacesRegistry spacesRegistry;
    }
}
