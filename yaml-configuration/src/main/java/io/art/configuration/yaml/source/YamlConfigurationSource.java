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

package io.art.configuration.yaml.source;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.dataformat.yaml.*;
import io.art.configuration.yaml.exception.*;
import io.art.core.collection.*;
import io.art.core.source.*;
import lombok.*;
import static com.fasterxml.jackson.databind.node.JsonNodeType.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableArray.*;
import static io.art.core.collection.ImmutableSet.*;
import static io.art.core.combiner.SectionCombiner.*;
import static io.art.core.constants.CompilerSuppressingWarnings.*;
import static io.art.core.constants.StringConstants.*;
import static java.util.Objects.*;
import static java.util.Spliterator.*;
import static java.util.Spliterators.*;
import static java.util.stream.StreamSupport.*;
import java.io.*;
import java.util.function.*;

@Getter
@AllArgsConstructor
public class YamlConfigurationSource implements NestedConfiguration {
    private static final YAMLMapper YAML_MAPPER = new YAMLMapper();

    private final String section;
    private final ModuleConfigurationSourceType type;
    private final Supplier<InputStream> inputStream;
    private JsonNode configuration;

    public YamlConfigurationSource(String section, ModuleConfigurationSourceType type, Supplier<InputStream> inputStream) {
        this.section = section;
        this.type = type;
        this.inputStream = inputStream;
        try {
            configuration = YAML_MAPPER.readTree(inputStream.get());
        } catch (IOException exception) {
            throw new YamlConfigurationLoadingException(exception);
        }
    }

    @Override
    public void refresh() {
        try {
            configuration = YAML_MAPPER.readTree(inputStream.get());
        } catch (IOException exception) {
            throw new YamlConfigurationLoadingException(exception);
        }
    }

    @Override
    @SneakyThrows
    public String dump() {
        return YAML_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(configuration);
    }

    @Override
    public Boolean asBool() {
        return let(configuration, JsonNode::asBoolean);
    }

    @Override
    public String asString() {
        return let(configuration, JsonNode::asText);
    }

    @Override
    public ImmutableArray<NestedConfiguration> asArray() {
        if (isNull(configuration)) return emptyImmutableArray();
        if (configuration.getNodeType() != ARRAY) {
            return emptyImmutableArray();
        }
        return stream(spliterator(configuration.elements(), configuration.size(), IMMUTABLE), false)
                .filter(YamlConfigurationSource::isValid)
                .map(node -> new YamlConfigurationSource(section, type, inputStream, node))
                .collect(immutableArrayCollector());
    }

    @Override
    public <T> ImmutableArray<T> asArray(Function<NestedConfiguration, T> mapper) {
        if (isNull(configuration)) return emptyImmutableArray();
        if (configuration.getNodeType() != ARRAY) {
            return emptyImmutableArray();
        }
        return stream(spliterator(configuration.elements(), configuration.size(), IMMUTABLE), false)
                .filter(YamlConfigurationSource::isValid)
                .map(node -> mapper.apply(new YamlConfigurationSource(section, type, inputStream, node)))
                .collect(immutableArrayCollector());
    }

    @Override
    public NestedConfiguration getNested(String path) {
        JsonNode configNode = getYamlConfigNode(path);
        return orNull(configNode, YamlConfigurationSource::isValid, node -> new YamlConfigurationSource(combine(section, path), type, inputStream, node));
    }

    @Override
    @SuppressWarnings(NULLABLE_PROBLEMS)
    public ImmutableSet<String> getKeys() {
        return stream(((Iterable<String>) configuration::fieldNames).spliterator(), false).collect(immutableSetCollector());
    }

    private JsonNode getYamlConfigNode(String path) {
        JsonNode yamlConfig = configuration;
        JsonNode node = yamlConfig.path(path);
        if (isValid(node)) return node;
        int dotIndex = path.indexOf(DOT);
        if (dotIndex == -1) {
            return MissingNode.getInstance();
        }
        node = yamlConfig.path(path.substring(0, dotIndex));
        path = path.substring(dotIndex + 1);
        while (true) {
            JsonNode valueNode = node.path(path);
            JsonNodeType valueNodeType = valueNode.getNodeType();
            switch (valueNodeType) {
                case OBJECT:
                case BINARY:
                case BOOLEAN:
                case NUMBER:
                case ARRAY:
                case STRING:
                    return valueNode;
                case MISSING:
                case POJO:
                case NULL:
                    break;
            }
            dotIndex = path.indexOf(DOT);
            if (dotIndex == -1) {
                return MissingNode.getInstance();
            }
            node = node.path(path.substring(0, dotIndex));
            path = path.substring(dotIndex + 1);
        }
    }

    private static boolean isValid(JsonNode node) {
        return !node.isNull() && !node.isMissingNode();
    }

}
