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

package ru.art.kafka.consumer.constants;

import static java.time.Duration.*;
import java.time.*;

public interface KafkaConsumerModuleConstants {
    String KAFKA_STREAMS_CLOSED = "Kafka stream: ''{0}'' closed";
    String KAFKA_CONSUMER_CLOSED = "Kafka consumer: ''{0}'' closed";
    String KAFKA_CONSUMER_MODULE_ID = "KAFKA_CONSUMER_MODULE";
    String KAFKA_CONSUMER_SERVICE_TYPE = "KAFKA_CONSUMER";
    Duration DEFAULT_DURATION = ofSeconds(10L);
    String KAFKA_TRACE_MESSAGE = "Kafka record: key = ''{0}'', value = ''{1}''";
    String KAFKA_KEY = "KAFKA_RECORD_KEY";
    String KAFKA_VALUE = "KAFKA_RECORD_VALUE";
    String TOPIC_IS_EMPTY = "topic is empty";
    String STREAM_ID_IS_EMPTY = "stream/applicationId is empty";
    String BROKERS_ARE_EMPTY = "brokers are empty";
    String CLIENT_ID_IS_EMPTY = "clientId is empty";
    String GROUP_ID_IS_EMPTY = "groupId is empty";
}
