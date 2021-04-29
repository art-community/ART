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

package io.art.configurator.configuration;

import io.art.configurator.model.*;
import io.art.configurator.source.*;
import io.art.core.collection.*;
import io.art.core.module.*;
import io.art.core.property.*;
import io.art.core.source.*;
import io.art.core.source.ConfigurationSource.*;
import lombok.*;
import static io.art.configurator.constants.ConfiguratorModuleConstants.ConfigurationSourceType.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.factory.ArrayFactory.*;
import static java.util.Comparator.*;

@Getter
public class ConfiguratorModuleConfiguration implements ModuleConfiguration {
    private ImmutableMap<ModuleConfigurationSourceType, ConfigurationSource> sources = emptyImmutableMap();
    private ImmutableMap<CustomConfigurationModel, Property<?>> customConfigurations = emptyImmutableMap();

    @Getter(lazy = true)
    private final ConfigurationSource delegate = new DelegateConfigurationSource(orderedSources());

    public PropertiesConfigurationSource getProperties() {
        return cast(sources.get(PROPERTIES));
    }

    public EnvironmentConfigurationSource getEnvironment() {
        return cast(sources.get(ENVIRONMENT));
    }

    public ImmutableArray<ConfigurationSource> orderedSources() {
        return immutableSortedArrayOf(getSources().values(), comparingInt((ConfigurationSource source) -> source.getType().getOrder()));
    }

    public ConfigurationSource getConfiguration() {
        return getDelegate();
    }

    public <T> T getCustomConfiguration(CustomConfigurationModel modelClass) {
        return cast(customConfigurations.get(modelClass).get());
    }

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<ConfiguratorModuleConfiguration, Configurator> {
        private final ConfiguratorModuleConfiguration configuration;

        @Override
        public Configurator from(ConfigurationSource source) {
            configuration.sources = ImmutableMap.<ModuleConfigurationSourceType, ConfigurationSource>immutableMapBuilder()
                    .putAll(configuration.sources)
                    .put(source.getType(), source)
                    .build();
            return this;
        }


        @Override
        public Configurator configure(ConfiguratorModuleConfiguration configuration) {
            ifNotEmpty(configuration.getCustomConfigurations(), configurations -> this.configuration.customConfigurations = configurations);
            return this;
        }
    }
}
