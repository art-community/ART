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

package io.art.yaml.configuration;

import com.fasterxml.jackson.dataformat.yaml.*;
import io.art.core.module.*;
import io.art.core.source.*;
import io.art.yaml.descriptor.*;
import lombok.*;

@Getter
public class YamlModuleConfiguration implements ModuleConfiguration {
    private final YAMLMapper objectMapper = new YAMLMapper();
    private final YamlReader reader = new YamlReader(objectMapper.getFactory());
    private final YamlWriter writer = new YamlWriter(objectMapper.getFactory());

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<YamlModuleConfiguration, Configurator> {
        private final YamlModuleConfiguration configuration;

        @Override
        public Configurator from(ConfigurationSource source) {
            return this;
        }
    }
}
