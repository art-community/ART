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

package io.art.kafka.consumer.module;

import lombok.*;
import org.apache.logging.log4j.*;
import io.art.core.module.Module;
import io.art.kafka.consumer.configuration.*;
import io.art.kafka.consumer.model.*;
import io.art.kafka.consumer.registry.*;
import io.art.kafka.consumer.specification.*;
import io.art.kafka.consumer.state.*;
import static java.text.MessageFormat.*;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.*;
import static org.apache.kafka.streams.KafkaStreams.State.NOT_RUNNING;
import static io.art.core.caster.Caster.*;
import static io.art.core.context.Context.*;
import static io.art.core.wrapper.ExceptionWrapper.*;
import static io.art.kafka.consumer.configuration.KafkaConsumerModuleConfiguration.*;
import static io.art.kafka.consumer.constants.KafkaConsumerModuleConstants.*;
import static io.art.logging.LoggingModule.*;
import static io.art.server.module.ServerModule.*;
import java.util.*;

@Getter
public class KafkaConsumerModule implements Module<KafkaConsumerModuleConfiguration, KafkaConsumerModuleState> {
    @Getter(lazy = true, onMethod = @__({@SuppressWarnings("unchecked")}), value = PRIVATE)
    private static final List<KafkaConsumerServiceSpecification> kafkaConsumerServices = cast(serviceModuleState()
            .getServiceRegistry()
            .getServices()
            .values()
            .stream()
            .filter(service -> service.getServiceTypes().contains(KAFKA_CONSUMER_SERVICE_TYPE))
            .map(service -> (KafkaConsumerServiceSpecification) service)
            .collect(toList()));
    @Getter(lazy = true, value = PRIVATE)
    private static final KafkaConsumerModuleConfiguration kafkaConsumerModule = context().getModule(KAFKA_CONSUMER_MODULE_ID, KafkaConsumerModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private static final KafkaConsumerModuleState kafkaConsumerModuleState = context().getModuleState(KAFKA_CONSUMER_MODULE_ID, KafkaConsumerModule::new);
    private final String id = KAFKA_CONSUMER_MODULE_ID;
    private final KafkaConsumerModuleConfiguration defaultConfiguration = DEFAULT_CONFIGURATION;
    private final KafkaConsumerModuleState state = new KafkaConsumerModuleState();
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = loggingModule().getLogger(KafkaConsumerModule.class);

    public static KafkaConsumerModuleConfiguration kafkaConsumerModule() {
        if (contextIsNotReady()) {
            return DEFAULT_CONFIGURATION;
        }
        return getKafkaConsumerModule();
    }

    public static KafkaConsumerModuleState kafkaConsumerModuleState() {
        return getKafkaConsumerModuleState();
    }

    public static List<KafkaConsumerServiceSpecification> kafkaConsumerServices() {
        return getKafkaConsumerServices();
    }

    public static KafkaStreamsRegistry kafkaStreamsRegistry() {
        return getKafkaConsumerModuleState().getKafkaStreamsRegistry();
    }

    @Override
    public void beforeUnload() {
        kafkaConsumerModuleState()
                .getKafkaConsumers()
                .entrySet()
                .stream()
                .filter(client -> !client.getValue().isStopped())
                .forEach(this::stopConsumer);
        kafkaConsumerModuleState()
                .getKafkaStreamsRegistry()
                .getStreams()
                .entrySet()
                .stream()
                .filter(client -> client.getValue().getKafkaStreams().state() != NOT_RUNNING)
                .forEach(this::stopStream);
    }

    private void stopConsumer(Map.Entry<String, ManagedKafkaConsumer> entry) {
        ignoreException(() -> {
            entry.getValue().getConsumer().close();
            getLogger().info(format(KAFKA_CONSUMER_CLOSED, entry.getKey()));
        }, getLogger()::error);
    }

    private void stopStream(Map.Entry<String, ManagedKafkaStream> entry) {
        ignoreException(() -> {
            entry.getValue().getKafkaStreams().close();
            getLogger().info(format(KAFKA_STREAMS_CLOSED, entry.getKey()));
        }, getLogger()::error);
    }
}
