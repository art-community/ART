/*
 *    Copyright 2019 ART
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.art.config.extensions.kafka;

public interface KafkaConfigKeys {
    String KAFKA_BROKER_SECTION_ID = "kafka.broker";
    String KAFKA_CONSUMER_SECTION_ID = "kafka.consumer";
    String KAFKA_TOPICS_SECTION_ID = "kafka.topics";
    String STREAMS_SECTION_ID = "streams";
    String KAFKA_PRODUCERS_SECTION_ID = "kafka.producers";
    String KAFKA_BROKER_ZOOKEEPER_SECTION_ID = "kafka.broker.zookeeper";
    String LOGS_DIRECTORY = "logsDirectory";
    String TICK_TIME = "tickTime";
    String MAXIMUM_CONNECTED_CLIENTS = "maximumConnectedClients";
    String INITIALIZATION_MODE = "initializationMode";
    String ZOOKEEPER_CONNECTION = "zookeeperConnection";
    String ADDITIONAL_PROPERTIES = "additionalProperties";
    String REPLICATION_FACTOR = "replicationFactor";
    String LOG_DIR = "log.dir";
    String BROKERS = "brokers";
    String KEY_DESERIALIZER = "keyDeserializer";
    String VALUE_DESERIALIZER = "valueDeserializer";
    String TOPICS = "topics";
    String RETENTION = "retention";
    String PARTITIONS = "partitions";
    String POLL_TIMEOUT = "pollTimeout";
    String GROUP_ID = "groupId";
    String KEY_SERDE = "keySerde";
    String VALUE_SERDE = "valueSerde";
    String TOPIC = "topic";
    String DELIVERY_TIMEOUT = "deliveryTimeout";
    String RETRIES = "retries";
    String KEY_SERIALIZER = "keySerializer";
    String VALUE_SERIALIZER = "valueSerializer";
}
