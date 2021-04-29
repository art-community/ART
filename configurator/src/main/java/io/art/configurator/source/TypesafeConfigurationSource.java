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

import com.typesafe.config.*;
import io.art.core.collection.*;
import io.art.core.source.*;
import lombok.*;
import static com.typesafe.config.ConfigFactory.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableArray.*;
import static io.art.core.combiner.SectionCombiner.*;
import static io.art.core.extensions.CollectionExtensions.*;
import static io.art.core.factory.ArrayFactory.*;
import static io.art.core.factory.SetFactory.*;
import static java.util.Objects.*;
import java.io.*;
import java.util.function.*;

@Getter
public class TypesafeConfigurationSource implements NestedConfiguration {
    private final String section;
    private final ModuleConfigurationSourceType type;
    private Supplier<InputStream> inputStream;
    private Config typesafeConfiguration;

    public TypesafeConfigurationSource(String section, ModuleConfigurationSourceType type, Supplier<InputStream> inputStream) {
        this.section = section;
        this.type = type;
        this.inputStream = inputStream;
        this.typesafeConfiguration = parseReader(new InputStreamReader(this.inputStream.get()));
    }

    public TypesafeConfigurationSource(String section, ModuleConfigurationSourceType type, Config typesafeConfiguration) {
        this.section = section;
        this.type = type;
        this.typesafeConfiguration = typesafeConfiguration;
    }

    @Override
    public void refresh() {
        apply(inputStream, file -> this.typesafeConfiguration = parseReader(new InputStreamReader(inputStream.get())));
    }

    @Override
    public String dump() {
        return typesafeConfiguration.root().render();
    }

    @Override
    public Boolean asBool() {
        return let(typesafeConfiguration, configuration -> configuration.getBoolean(section));
    }

    @Override
    public String asString() {
        return let(typesafeConfiguration, configuration -> configuration.getString(section));
    }

    @Override
    public NestedConfiguration getNested(String path) {
        Config configuration = this.typesafeConfiguration.atPath(path);
        if (isNull(configuration) || !this.typesafeConfiguration.hasPath(path) || configuration.isEmpty()) {
            return null;
        }
        return new TypesafeConfigurationSource(combine(section, path), type, configuration);
    }

    @Override
    public ImmutableArray<NestedConfiguration> asArray() {
        return orEmptyImmutableArray(section, typesafeConfiguration::hasPath, path -> immutableArrayOf(typesafeConfiguration.getConfigList(path)))
                .stream()
                .map(config -> new TypesafeConfigurationSource(section, type, config))
                .collect(immutableArrayCollector());
    }

    @Override
    public <T> ImmutableArray<T> asArray(Function<NestedConfiguration, T> mapper) {
        return orEmptyImmutableArray(section, typesafeConfiguration::hasPath, path -> immutableArrayOf(typesafeConfiguration.getConfigList(path)))
                .stream()
                .map(config -> mapper.apply(new TypesafeConfigurationSource(section, type, config)))
                .collect(immutableArrayCollector());
    }

    @Override
    public ImmutableSet<String> getKeys() {
        return immutableSetOf(typesafeConfiguration.root().keySet());
    }
}
