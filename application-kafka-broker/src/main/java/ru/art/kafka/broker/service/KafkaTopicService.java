package ru.art.kafka.broker.service;

import kafka.admin.RackAwareMode;
import kafka.log.LogConfig;
import ru.art.kafka.broker.api.model.KafkaTopic;

import java.util.Properties;

import static ru.art.kafka.broker.constants.KafkaBrokerModuleConstants.DEFAULT_TOPIC_REPLICATION_FACTOR;
import static ru.art.kafka.broker.module.KafkaBrokerModule.kafkaBrokerModuleState;

public interface KafkaTopicService {
    /**
     *
     * @param topic -
     */
    static void addTopic(KafkaTopic topic) {
        Properties topicProperties = new Properties();
        topicProperties.put(LogConfig.RetentionMsProp(), String.valueOf(topic.getProperties().getRetention()));
        kafkaBrokerModuleState().getBroker().getAdminZookeeperClient().createTopic(topic.getTopic(),
                topic.getProperties().getPartitions(),
                topic.getProperties().getReplicationFactor(),
                topicProperties,
                RackAwareMode.Disabled$.MODULE$);
    }

    // topic + num of partitions
    static void addPartitions() {

    }

    /**
     * If topic exists at broker, delete it.
     * @param topic - topic for deletion;
     */
    static void deleteTopic(KafkaTopic topic) {
        if (!kafkaBrokerModuleState().getBroker().getServer().zkClient().topicExists(topic.getTopic())) return;
       kafkaBrokerModuleState().getBroker().getAdminZookeeperClient().deleteTopic(topic.getTopic());
    }
}
