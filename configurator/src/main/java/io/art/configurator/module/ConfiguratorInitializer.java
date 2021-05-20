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

package io.art.configurator.module;

import io.art.configurator.configuration.*;
import io.art.configurator.custom.*;
import io.art.configurator.model.*;
import io.art.core.annotation.*;
import io.art.core.collection.*;
import io.art.core.module.*;
import io.art.core.property.*;
import lombok.*;
import static java.util.Objects.*;

@UsedByGenerator
public class ConfiguratorInitializer implements ModuleInitializer<ConfiguratorModuleConfiguration, ConfiguratorModuleConfiguration.Configurator, ConfiguratorModule> {
    private CustomConfigurationRegistry registry = new CustomConfigurationRegistry();

    public ConfiguratorInitializer registry(CustomConfigurationRegistry registry) {
        this.registry = registry;
        return this;
    }

    public ConfiguratorModuleConfiguration initialize(ConfiguratorModule module) {
        if (isNull(registry)) registry = new CustomConfigurationRegistry();
        return new Initial(registry.configure(module.orderedSources()));
    }

    @Getter
    @RequiredArgsConstructor
    private static class Initial extends ConfiguratorModuleConfiguration {
        private final ImmutableMap<CustomConfigurationModel, Property<?>> customConfigurations;
    }
}
