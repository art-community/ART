package ru.art.kafka.broker.service;

import kafka.admin.RackAwareMode;
import kafka.log.LogConfig;
import ru.art.kafka.broker.api.model.KafkaTopic;
import ru.art.kafka.broker.api.model.KafkaTopicResult;
import ru.art.kafka.broker.api.model.TopicPartitions;
import scala.Option;
import scala.collection.Seq;

import java.util.List;
import java.util.Properties;

import static ru.art.core.checker.CheckerForEmptiness.isEmpty;
import static ru.art.kafka.broker.api.converter.ScalaToJavaConverter.seqToList;
import static ru.art.kafka.broker.api.operations.KafkaTopicResultsFactory.*;
import static ru.art.kafka.broker.constants.KafkaBrokerModuleConstants.*;
import static ru.art.kafka.broker.constants.KafkaBrokerModuleConstants.KafkaServiceErrors.TOPIC_NOT_EXISTS;
import static ru.art.kafka.broker.module.KafkaBrokerModule.kafkaBrokerModuleState;
import static ru.art.kafka.broker.operations.KafkaTopicServiceOperations.getAdminZookeeperClient;
import static ru.art.kafka.broker.operations.KafkaTopicServiceOperations.getExistingAssignmentForTopic;

public interface KafkaTopicService {
    /**
     * if topic's properties are empty, topic's created with default properties;
     * @param topic - topic's name and properties (optionally) to create.
     */
    static  <T> KafkaTopicResult<T> addTopic(KafkaTopic topic) {
        Properties topicProperties = new Properties();
        if (isEmpty(topic.getProperties())) {
            topicProperties.put(LogConfig.RetentionMsProp(), String.valueOf(DEFAULT_TOPIC_RETENTION));
            getAdminZookeeperClient().createTopic(topic.getTopic(),
                    DEFAULT_TOPIC_PARTITIONS,
                    DEFAULT_TOPIC_REPLICATION_FACTOR,
                    topicProperties,
                    RackAwareMode.Disabled$.MODULE$);
            return createOkEmptyResponse();
        }
        topicProperties.put(LogConfig.RetentionMsProp(), String.valueOf(topic.getProperties().getRetentionMs()));
        getAdminZookeeperClient().createTopic(topic.getTopic(),
                topic.getProperties().getPartitions(),
                topic.getProperties().getReplicationFactor(),
                topicProperties,
                RackAwareMode.Disabled$.MODULE$);
        return createOkEmptyResponse();
    }

    /**
     * Method adds partition to topic.
     * @param add - consists topic name and number of adding partitions;
     * @return - error, if topic doesn't exists.
     */
    static <T> KafkaTopicResult<T> addPartitions(TopicPartitions add) {
        if (!kafkaBrokerModuleState().getBroker().getServer().zkClient().topicExists(add.getTopic())) {
            return createErrorResponse(String.format(TOPIC_NOT_EXISTS, add.getTopic()));
        }
        getAdminZookeeperClient().addPartitions(add.getTopic(),
                getExistingAssignmentForTopic(add.getTopic()),
                getAdminZookeeperClient().getBrokerMetadatas(RackAwareMode.Enforced$.MODULE$, scala.Option.apply(null)),
                add.getNumberOfPartitions(),
                Option.empty(),
                false);
        return createOkEmptyResponse();
    }

    /**
     * If topic exists at broker, delete it.
     * @param topic - topic for deletion;
     * @return - error, if topic doesn't exists.
     */

    static <T> KafkaTopicResult<T> deleteTopic(KafkaTopic topic) {
        if (!kafkaBrokerModuleState().getBroker().getServer().zkClient().topicExists(topic.getTopic())) {
            return createErrorResponse(String.format(TOPIC_NOT_EXISTS, topic.getTopic()));
        }
        getAdminZookeeperClient().deleteTopic(topic.getTopic());
        return createOkEmptyResponse();
    }

    /**
     * @return ALL (and marked for deletion too!) topics in cluster of brokers, if no topics returns empty list.
     */
    static KafkaTopicResult<List<String>> getAllTopics() {
        Seq<String> topicSeq =  kafkaBrokerModuleState().getBroker().getServer().zkClient().getAllTopicsInCluster();
        return createOkResponse(seqToList(topicSeq));
    }

    /**
     * @return only ACTUAL topics, without marked for deletion;
     */
    static KafkaTopicResult<List<String>> getActualTopics() {
        List<String> allTopics =  seqToList(kafkaBrokerModuleState().getBroker().getServer().zkClient().getAllTopicsInCluster());
        allTopics.removeIf(topic -> kafkaBrokerModuleState().getBroker().getServer().zkClient().isTopicMarkedForDeletion(topic));
        return createOkResponse(allTopics);
    }
}
