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

package ru.art.kafka.broker.embedded;

import kafka.admin.RackAwareMode;
import kafka.log.LogConfig;
import kafka.server.*;
import kafka.zk.AdminZkClient;
import lombok.*;
import ru.art.kafka.broker.configuration.*;
import scala.collection.immutable.List;
import static java.util.Objects.*;
import static kafka.server.KafkaConfig.*;
import static lombok.AccessLevel.*;
import static org.apache.kafka.common.utils.Time.*;
import static ru.art.core.checker.CheckerForEmptiness.isNotEmpty;
import static ru.art.core.constants.NetworkConstants.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.kafka.broker.constants.KafkaBrokerModuleConstants.*;
import static ru.art.kafka.broker.constants.KafkaBrokerModuleConstants.ZookeeperInitializationMode.*;
import static ru.art.kafka.broker.embedded.EmbeddedZookeeper.*;
import static ru.art.kafka.broker.module.KafkaBrokerModule.*;
import static scala.Option.*;
import java.util.*;

@Getter
@Builder(access = PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class EmbeddedKafkaBroker {
    private final KafkaBrokerConfiguration kafkaBrokerConfiguration;
    private final ZookeeperConfiguration zookeeperConfiguration;
    private final ZookeeperInitializationMode zookeeperInitializationMode;
    private final KafkaServer server;
    private EmbeddedZookeeper embeddedZookeeper;
    private AdminZkClient adminZookeeperClient;

    /**
     *
     * @param kafkaBrokerConfiguration - custom broker configuration to fill properties instance to start KafkaService;
     * @param zookeeperConfiguration - custom zookeeper configuration to start new or use existing Zookeeper;
     * @param zookeeperInitializationMode - parameter allows different ways of choosing Zookeeper;
     * if is not equals ON_KAFKA_BROKER_INITIALIZATION value, Zookeeper is not started, as soon as expected it's running already;
     * if mode is ON_KAFKA_BROKER_INITIALIZATION and default topics define in config, create them with replication factor = 1;
     * @return broker instance;
     */
    public static EmbeddedKafkaBroker startKafkaBroker(KafkaBrokerConfiguration kafkaBrokerConfiguration, ZookeeperConfiguration zookeeperConfiguration, ZookeeperInitializationMode zookeeperInitializationMode) {
        Properties properties = new Properties();
        properties.put(ZkConnectProp(), kafkaBrokerConfiguration.getZookeeperConnection());
        properties.put(ListenersProp(), PLAINTEXT + LOCALHOST_IP_ADDRESS + COLON + kafkaBrokerConfiguration.getPort());
        properties.put(LogDirProp(), kafkaBrokerConfiguration.getLogDirectory());
        properties.put(OffsetsTopicReplicationFactorProp(), kafkaBrokerConfiguration.getReplicationFactor());
        properties.putAll(kafkaBrokerConfiguration.getAdditionalProperties());
        KafkaServer kafkaServer = new KafkaServer(new KafkaConfig(properties), SYSTEM, empty(), List.empty());
        if (zookeeperInitializationMode == ON_KAFKA_BROKER_INITIALIZATION) {
            EmbeddedKafkaBroker broker = builder().kafkaBrokerConfiguration(kafkaBrokerConfiguration)
                    .zookeeperConfiguration(zookeeperConfiguration)
                    .zookeeperInitializationMode(zookeeperInitializationMode)
                    .server(kafkaServer)
                    .embeddedZookeeper(startZookeeper(zookeeperConfiguration))
                    .build();
            kafkaBrokerModuleState().setBroker(broker);
            kafkaServer.startup();
            broker.setAdminZookeeperClient(new AdminZkClient(kafkaServer.zkClient()));

            if (isNotEmpty(zookeeperConfiguration.getKafkaDefaultTopics())) {
                for (Map.Entry<String, KafkaTopicProperties> topic: zookeeperConfiguration.getKafkaDefaultTopics().entrySet()) {
                    Properties topicProperties = new Properties();
                    topicProperties.put(LogConfig.RetentionMsProp(), String.valueOf(topic.getValue().getRetention()));
                    broker.getAdminZookeeperClient().createTopic(topic.getKey(), topic.getValue().getPartitions(), DEFAULT_TOPIC_REPLICATION_FACTOR, topicProperties, RackAwareMode.Disabled$.MODULE$);
                }
            }
            return broker;
        }
        EmbeddedKafkaBroker broker = builder().kafkaBrokerConfiguration(kafkaBrokerConfiguration)
                .zookeeperConfiguration(zookeeperConfiguration)
                .zookeeperInitializationMode(zookeeperInitializationMode)
                .server(kafkaServer)
                .build();
        kafkaBrokerModuleState().setBroker(broker);
        kafkaServer.startup();
        return broker;
    }

    /**
     * Kafka zookeeper's configuration is taking from module;
     * @param configuration - custom broker configuration to fill properties instance to start KafkaService;
     * @param zookeeperInitializationMode - parameter allows different ways of choosing Zookeeper;
     * @return broker instance;
     */
    public static EmbeddedKafkaBroker startKafkaBroker(KafkaBrokerConfiguration configuration, ZookeeperInitializationMode zookeeperInitializationMode) {
        return startKafkaBroker(configuration, kafkaBrokerModule().getZookeeperConfiguration(), zookeeperInitializationMode);
    }

    /**
     * Kafka zookeeper's configuration and initialization mode are taking from module;
     * @param configuration - custom broker configuration to fill properties instance to start KafkaService;
     * @return broker instance;
     */
    public static EmbeddedKafkaBroker startKafkaBroker(KafkaBrokerConfiguration configuration) {
        return startKafkaBroker(configuration, kafkaBrokerModule().getZookeeperConfiguration(), kafkaBrokerModule().getZookeeperInitializationMode());
    }

    /**
     * Kafka broker's configuration's taking from module;
     * @return broker instance;
     */
    public static EmbeddedKafkaBroker startKafkaBroker() {
        return startKafkaBroker(kafkaBrokerModule().getKafkaBrokerConfiguration());
    }

    public void await() {
        server.awaitShutdown();
    }

    public void shutdown() {
        server.shutdown();
        server.awaitShutdown();
    }

    public void restart() {
        shutdown();
        startKafkaBroker(kafkaBrokerConfiguration, zookeeperConfiguration, MANUAL);
    }

    public void restartWithZookeeper() {
        shutdown();
        if (nonNull(embeddedZookeeper)) {
            embeddedZookeeper.restart();
        }
        startKafkaBroker(kafkaBrokerConfiguration, zookeeperConfiguration, MANUAL);
    }

    public boolean isWorking() {
        return server.brokerState().currentState() == RunningAsBroker.state();
    }
}
