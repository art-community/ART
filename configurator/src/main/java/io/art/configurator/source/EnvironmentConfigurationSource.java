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
import lombok.*;
import static io.art.configurator.constants.ConfiguratorModuleConstants.ConfigurationSourceType.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableArray.*;
import static io.art.core.collection.ImmutableSet.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.context.Context.*;
import static io.art.core.factory.ArrayFactory.*;
import static io.art.core.factory.SetFactory.*;
import static io.art.core.handler.ExceptionHandler.*;
import static java.lang.Integer.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import java.util.*;
import java.util.function.*;

@Getter
@AllArgsConstructor
public class EnvironmentConfigurationSource implements NestedConfiguration {
    private final String section;
    private final ModuleConfigurationSourceType type = ENVIRONMENT;
    private final String path = type.toString();
    private final ImmutableMap<String, String> environment = context().configuration().getEnvironment();

    @Override
    public ImmutableSet<String> getKeys() {
        if (isEmpty(section)) {
            return immutableSetOf(context().configuration().getEnvironment().keySet());
        }
        return environment.keySet().stream().filter(key -> key.startsWith(section + DASH)).collect(immutableSetCollector());
    }

    @Override
    public NestedConfiguration getNested(String path) {
        String newSection = path.replace(DOT, DASH);
        if (getKeys().contains(newSection)) {
            return new EnvironmentConfigurationSource(newSection);
        }
        return null;
    }

    @Override
    public String dump() {
        return environment.entrySet().stream().map(entry -> entry.getKey() + EQUAL + entry.getValue()).collect(joining(NEW_LINE));
    }

    @Override
    public Boolean asBool() {
        return let(environment.get(section), Boolean::valueOf);
    }

    @Override
    public String asString() {
        return environment.get(section);
    }

    @Override
    public ImmutableArray<NestedConfiguration> asArray() {
        List<NestedConfiguration> array = dynamicArray();
        for (String key : environment.keySet()) {
            if (isEmpty(section) || key.startsWith(section)) {
                Integer index = nullIfException(() -> parseInt(key.substring(key.lastIndexOf(DASH) + 1)));
                if (nonNull(index)) {
                    array.add(index, getNested(key));
                }
            }
        }
        return immutableArrayOf(array);
    }

    @Override
    public <T> ImmutableArray<T> asArray(Function<NestedConfiguration, T> mapper) {
        return asArray().stream().map(mapper).collect(immutableArrayCollector());
    }
}
