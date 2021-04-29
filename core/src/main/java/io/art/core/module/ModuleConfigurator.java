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

package io.art.core.module;

import io.art.core.collection.*;
import io.art.core.source.*;
import static io.art.core.caster.Caster.*;
import java.util.*;

public interface ModuleConfigurator<Configuration extends ModuleConfiguration, Configurator extends ModuleConfigurator<Configuration, Configurator>> {
    default Configurator from(ConfigurationSource source) {
        return cast(this);
    }

    default Configurator from(ImmutableArray<ConfigurationSource> sources) {
        sources.forEach(this::from);
        return cast(this);
    }

    default Configurator from(Collection<ConfigurationSource> sources) {
        sources.forEach(this::from);
        return cast(this);
    }

    default Configurator configure(Configuration configuration) {
        return cast(this);
    }
}
