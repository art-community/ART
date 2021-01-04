/*
 * ART Java
 *
 * Copyright 2019 ART
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

package ru.art.kafka.consumer.registry;

import lombok.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.logging.log4j.*;
import ru.art.kafka.consumer.configuration.*;
import ru.art.kafka.consumer.container.*;
import ru.art.kafka.consumer.exception.*;
import ru.art.kafka.consumer.model.*;
import static java.lang.String.*;
import static java.text.MessageFormat.format;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.kafka.streams.StreamsConfig.*;
import static org.apache.logging.log4j.ThreadContext.*;
import static ru.art.core.checker.CheckerForEmptiness.isEmpty;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.core.wrapper.ExceptionWrapper.*;
import static ru.art.kafka.consumer.constants.KafkaConsumerModuleConstants.*;
import static ru.art.kafka.consumer.module.KafkaConsumerModule.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.logging.ThreadContextExtensions.*;
import java.util.*;
import java.util.function.*;

@Getter
public class KafkaStreamsRegistry {
    private final Map<String, ManagedKafkaStream> streams = concurrentHashMap();
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = loggingModule().getLogger(KafkaStreamsRegistry.class);

    public static <Key, Value> KStream<Key, Value> withTracing(KStream<Key, Value> stream) {
        return stream.peek(KafkaStreamsRegistry::logKafkaRecord);
    }

    private static <K, V> void logKafkaRecord(K key, V value) {
        if (!kafkaConsumerModule().isEnableTracing()) {
            return;
        }
        putIfNotNull(KAFKA_KEY, key);
        putIfNotNull(KAFKA_VALUE, value);
        getLogger().info(format(KAFKA_TRACE_MESSAGE, key, value));
        remove(KAFKA_KEY);
        remove(KAFKA_VALUE);
    }

    @SuppressWarnings("Duplicates")
    public KafkaStreamsRegistry createStream(String streamId, Function<StreamsBuilder, KafkaStreamContainer> streamCreator) {
        if (isEmpty(streamId)) throw new KafkaConsumerModuleException(STREAM_ID_IS_EMPTY);
        StreamsBuilder builder = new StreamsBuilder();
        KafkaStreamContainer streamContainer = streamCreator.apply(builder);
        KafkaStreamConfiguration configuration = getOrElse(streamContainer.getConfiguration(), kafkaConsumerModule()
                .getKafkaStreamConfigurations()
                .get(streamId));
        if (isEmpty(configuration.getBrokers())) throw new KafkaConsumerModuleException(BROKERS_ARE_EMPTY);
        streams.put(streamId, ManagedKafkaStream.builder()
                .kafkaStreams(new KafkaStreams(builder.build(), createProperties(streamId, configuration)))
                .builder(builder)
                .configuration(configuration)
                .build());
        return this;
    }

    @SuppressWarnings("Duplicates")
    public <K, V> KafkaStreamsRegistry registerStream(String streamId, Function<KStream<K, V>, KStream<?, ?>> streamCreator) {
        if (isEmpty(streamId)) throw new KafkaConsumerModuleException(STREAM_ID_IS_EMPTY);
        StreamsBuilder builder = new StreamsBuilder();
        KafkaStreamConfiguration configuration = kafkaConsumerModule()
                .getKafkaStreamConfigurations()
                .get(streamId);
        if (isEmpty(configuration.getBrokers())) throw new KafkaConsumerModuleException(BROKERS_ARE_EMPTY);
        if (isEmpty(configuration.getTopic())) throw new KafkaConsumerModuleException(TOPIC_IS_EMPTY);
        streamCreator.apply(withTracing(builder.stream(configuration.getTopic())));
        streams.put(streamId, ManagedKafkaStream.builder()
                .kafkaStreams(new KafkaStreams(builder.build(), createProperties(streamId, configuration)))
                .builder(builder)
                .configuration(configuration)
                .build());
        return this;
    }

    private Properties createProperties(String streamId, KafkaStreamConfiguration configuration) {
        Properties properties = new Properties();
        properties.put(APPLICATION_ID_CONFIG, streamId);
        properties.put(BOOTSTRAP_SERVERS_CONFIG, join(COMMA, configuration.getBrokers()));
        properties.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, configuration.getKeySerde().getClass());
        properties.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, configuration.getValueSerde().getClass());
        properties.putAll(configuration.getAdditionalProperties());
        return properties;
    }

    public void refreshStreams() {
        streams.values()
                .stream()
                .filter(stream -> stream.getKafkaStreams().state().isRunning())
                .forEach(stream -> ignoreException(() -> stream.getKafkaStreams().close()));
        streams.replaceAll((streamId, stream) -> ManagedKafkaStream.builder()
                .kafkaStreams(new KafkaStreams(stream.getBuilder().build(), createProperties(streamId, stream.getConfiguration())))
                .builder(stream.getBuilder())
                .build());
    }
}

