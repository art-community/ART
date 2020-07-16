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

package io.art.json.configuration;

import com.fasterxml.jackson.databind.*;
import io.art.core.module.*;
import lombok.*;
import static com.fasterxml.jackson.core.JsonParser.Feature.*;
import static io.art.core.extensions.NullCheckingExtensions.*;

@Getter
public class JsonModuleConfiguration implements ModuleConfiguration {
    ObjectMapper objectMapper = new ObjectMapper();

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<Configurator> {
        private final JsonModuleConfiguration configuration;

        @Override
        public Configurator from(ModuleConfigurationSource source) {
            let(source.getBool("json.allowComments"), value -> configuration.objectMapper.configure(ALLOW_COMMENTS, value));
            return this;
        }
    }
}
