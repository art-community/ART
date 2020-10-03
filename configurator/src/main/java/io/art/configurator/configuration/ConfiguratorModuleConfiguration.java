/*
 * ART
 *
 * Copyright 2020 ART
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

import com.google.common.collect.*;
import io.art.configurator.source.*;
import io.art.core.module.*;
import io.art.core.module.ModuleConfigurationSource.*;
import lombok.*;
import static com.google.common.collect.ImmutableMap.*;
import static com.google.common.collect.Ordering.*;
import static io.art.configurator.constants.ConfiguratorModuleConstants.ConfigurationSourceType.*;
import static io.art.core.caster.Caster.*;
import static java.util.Comparator.comparingInt;

@Getter
public class ConfiguratorModuleConfiguration implements ModuleConfiguration {
    private ImmutableMap<ModuleConfigurationSourceType, ModuleConfigurationSource> sources = of();

    public PropertiesConfigurationSource getProperties() {
        return cast(sources.get(PROPERTIES));
    }

    public EnvironmentConfigurationSource getEnvironment() {
        return cast(sources.get(ENVIRONMENT));
    }

    public ImmutableList<ModuleConfigurationSource> orderedSources() {
        return from(comparingInt((ModuleConfigurationSource source) -> source.getType().getOrder())).immutableSortedCopy(getSources().values());
    }

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<ConfiguratorModuleConfiguration, Configurator> {
        private final ConfiguratorModuleConfiguration configuration;

        @Override
        public Configurator from(ModuleConfigurationSource source) {
            configuration.sources = ImmutableMap.<ModuleConfigurationSourceType, ModuleConfigurationSource>builder()
                    .putAll(configuration.sources)
                    .put(source.getType(), source)
                    .build();
            return this;
        }

    }
}