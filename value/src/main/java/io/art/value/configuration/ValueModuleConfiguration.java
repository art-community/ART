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

package io.art.value.configuration;

import io.art.core.module.*;
import io.art.value.immutable.Value;
import io.art.value.mapper.*;
import io.art.value.registry.*;
import lombok.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.checker.NullityChecker.*;
import java.lang.reflect.*;

@Getter
public class ValueModuleConfiguration implements ModuleConfiguration {
    private ValueMapperRegistry registry = new ValueMapperRegistry();

    public <T> ValueMapper<T, ? extends Value> getMapper(Type type) {
        return cast(registry.get(type));
    }

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<ValueModuleConfiguration, Configurator> {
        private final ValueModuleConfiguration configuration;

        @Override
        public Configurator initialize(ValueModuleConfiguration configuration) {
            apply(configuration.getRegistry(), registry -> this.configuration.registry = registry);
            return this;
        }
    }

}
