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

package io.art.configurator.source;

import io.art.core.collection.*;
import io.art.core.source.*;
import io.art.value.immutable.Value;
import lombok.*;
import static io.art.configurator.constants.ConfiguratorModuleConstants.ConfigurationSourceType.*;
import static io.art.core.collection.ImmutableArray.*;
import static io.art.core.collection.ImmutableSet.*;
import static io.art.core.combiner.SectionCombiner.*;
import static io.art.core.extensions.StringExtensions.*;
import static io.art.value.immutable.Value.*;
import static io.art.value.mapping.PrimitiveMapping.toString;
import static java.util.Objects.*;
import java.util.function.*;

@Getter
@RequiredArgsConstructor
public class ValueConfigurationSource implements NestedConfiguration {
    private final String section;
    private final ModuleConfigurationSourceType type = ENTITY;
    private final String path = type.toString();
    private final Value value;

    @Override
    public Boolean asBool() {
        if (!isPrimitive(value)) {
            return null;
        }
        return asPrimitive(value).getBool();
    }

    @Override
    public String asString() {
        if (!isPrimitive(value)) {
            return null;
        }
        return asPrimitive(value).getString();
    }

    @Override
    public ImmutableArray<NestedConfiguration> asArray() {
        if (!isArray(value)) {
            return emptyImmutableArray();
        }
        return Value.asArray(value).asImmutableArray(element -> new ValueConfigurationSource(section, element));
    }

    @Override
    public <T> ImmutableArray<T> asArray(Function<NestedConfiguration, T> mapper) {
        if (!isArray(value)) {
            return emptyImmutableArray();
        }
        return Value.asArray(value).asImmutableArray(element -> mapper.apply(new ValueConfigurationSource(section, element)));
    }

    @Override
    public NestedConfiguration getNested(String path) {
        if (!isEntity(value)) {
            return null;
        }
        Value nested = asEntity(value).find(path);
        if (isNull(nested)) {
            return null;
        }
        return new ValueConfigurationSource(combine(section, path), asEntity(nested));
    }

    @Override
    public String dump() {
        return emptyIfNull(value);
    }


    @Override
    public ImmutableSet<String> getKeys() {
        if (!isEntity(value)) {
            return emptyImmutableSet();
        }
        return asEntity(value).asMap().keySet().stream().map(toString::map).collect(immutableSetCollector());
    }
}
