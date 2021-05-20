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
import static io.art.core.constants.StringConstants.*;
import static io.art.core.extensions.CollectionExtensions.*;
import static io.art.core.factory.ArrayFactory.*;
import static io.art.core.factory.SetFactory.*;
import java.io.*;
import java.util.function.*;

@Getter
public class TypesafeConfigurationSource implements NestedConfiguration {
    private final ConfigurationSourceParameters parameters;
    private final String section;
    private final String path;
    private final ModuleConfigurationSourceType type;
    private Supplier<InputStream> inputStream;
    private Config typesafeConfiguration;

    public TypesafeConfigurationSource(ConfigurationSourceParameters parameters) {
        this.parameters = parameters;
        this.section = parameters.getSection();
        this.path = parameters.getPath();
        this.type = parameters.getType();
        this.inputStream = parameters.getInputStream();
        this.typesafeConfiguration = parseReader(new InputStreamReader(this.inputStream.get()));
    }

    public TypesafeConfigurationSource(ConfigurationSourceParameters parameters, Config typesafeConfiguration) {
        this.parameters = parameters;
        this.section = parameters.getSection();
        this.type = parameters.getType();
        this.path = parameters.getPath();
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
        String newPath = combine(section, path);
        if (!this.typesafeConfiguration.hasPath(newPath)) {
            return null;
        }
        return new TypesafeConfigurationSource(parameters.toBuilder().section(newPath).build(), typesafeConfiguration);
    }

    @Override
    public ImmutableArray<NestedConfiguration> asArray() {
        return orEmptyImmutableArray(section, typesafeConfiguration::hasPath, path -> immutableArrayOf(typesafeConfiguration.getConfigList(path)))
                .stream()
                .map(config -> new TypesafeConfigurationSource(parameters, config))
                .collect(immutableArrayCollector());
    }

    @Override
    public <T> ImmutableArray<T> asArray(Function<NestedConfiguration, T> mapper) {
        return orEmptyImmutableArray(section, typesafeConfiguration::hasPath, path -> immutableArrayOf(typesafeConfiguration.getConfigList(path)))
                .stream()
                .map(config -> mapper.apply(new TypesafeConfigurationSource(parameters.toBuilder().section(EMPTY_STRING).build(), config)))
                .collect(immutableArrayCollector());
    }

    @Override
    public ImmutableSet<String> getKeys() {
        return immutableSetOf(typesafeConfiguration.root().keySet());
    }
}
