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

import io.art.core.module.*;
import lombok.experimental.*;
import static java.util.function.UnaryOperator.*;
import java.util.function.*;

@UtilityClass
public class ConfiguratorActivator {
    public static ModuleActivator configurator() {
        return configurator(identity());
    }

    public static ModuleActivator configurator(UnaryOperator<ConfiguratorInitializer> initializer) {
        return ModuleActivator.builder()
                .id(ConfiguratorModule.class.getSimpleName())
                .initializer(() -> initializer.apply(new ConfiguratorInitializer()))
                .factory(ConfiguratorModule::new)
                .build();
    }

}
