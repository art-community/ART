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

package io.art.value.module;

import io.art.core.annotation.*;
import io.art.core.module.*;
import io.art.value.configuration.*;
import io.art.value.registry.*;
import lombok.*;

@UsedByGenerator
public class ValueInitializer implements ModuleInitializer<ValueModuleConfiguration, ValueModuleConfiguration.Configurator, ValueModule> {
    private final Initial configuration = new Initial();

    public ValueInitializer registry(ValueMapperRegistry registry) {
        configuration.registry = registry;
        return this;
    }

    @Override
    public ValueModuleConfiguration initialize(ValueModule module) {
        return configuration;
    }

    @Getter
    private static class Initial extends ValueModuleConfiguration {
        private ValueMapperRegistry registry = new ValueMapperRegistry();
    }
}
