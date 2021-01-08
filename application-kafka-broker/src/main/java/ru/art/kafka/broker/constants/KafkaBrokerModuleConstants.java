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

package ru.art.kafka.broker.constants;

import static java.io.File.separator;
import static ru.art.core.network.selector.PortSelector.*;

public interface KafkaBrokerModuleConstants {
    String KAFKA_BROKER_MODULE_ID = "KAFKA_BROKER_MODULE";
    String DEFAULT_ZOOKEEPER_SNAPSHOTS_DIRECTORY = "zookeeper" + separator + "snapshots";
    String ZOOKEEPER_PREFIX = "zookeeper-";
    String DEFAULT_ZOOKEEPER_LOGS_DIRECTORY = "zookeeper" + separator + "logs";
    String DEFAULT_KAFKA_LOG_DIRECTORY = "kafka" + separator + "logs";
    int DEFAULT_ZOOKEEPER_PORT = findAvailableTcpPort();
    int DEFAULT_BROKER_PORT = findAvailableTcpPort();
    int DEFAULT_ZOOKEEPER_MAXIMUM_CONNECTED_CLIENTS = 1024;
    int DEFAULT_ZOOKEEPER_TICK_TIME = 500;
    int DEFAULT_TOPIC_REPLICATION_FACTOR = 1;
    int DEFAULT_TOPIC_PARTITIONS = 3;
    long DEFAULT_TOPIC_RETENTION = 604800000;
    String PLAINTEXT = "PLAINTEXT://";

    enum ZookeeperInitializationMode {
        ON_KAFKA_BROKER_INITIALIZATION,
        MANUAL
    }

    interface KafkaServiceErrors {
        String TOPIC_NOT_EXISTS = "Topic %s doesn't exists in Kafka cluster!";
    }
}
