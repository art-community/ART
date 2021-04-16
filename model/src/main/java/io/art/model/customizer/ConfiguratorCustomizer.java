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

import io.art.configurator.configuration.*;
import io.art.configurator.custom.*;
import io.art.configurator.model.*;
import io.art.core.annotation.*;
import io.art.core.collection.*;
import io.art.core.property.*;
import io.art.core.source.*;
import lombok.*;
import static java.util.Objects.isNull;

@UsedByGenerator
public class ConfiguratorCustomizer {
    private CustomConfigurationRegistry registry = new CustomConfigurationRegistry();

    public ConfiguratorCustomizer registry(CustomConfigurationRegistry registry) {
        this.registry = registry;
        return this;
    }

    public ConfiguratorModuleConfiguration configure(ImmutableArray<ConfigurationSource> sources) {
        if (isNull(registry)) registry = new CustomConfigurationRegistry();
        return new Custom(registry.configure(sources));
    }

    @Getter
    @RequiredArgsConstructor
    private static class Custom extends ConfiguratorModuleConfiguration {
        private final ImmutableMap<CustomConfigurationModel, Property<?>> customConfigurations;
    }
}
