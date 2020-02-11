package ru.art.kafka.broker.operations;

import kafka.zk.AdminZkClient;
import scala.collection.Map;
import scala.collection.Seq;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.art.kafka.broker.api.converter.ScalaToJavaConverter.JavaSetToScalaImmutableSet;
import static ru.art.kafka.broker.module.KafkaBrokerModule.kafkaBrokerModuleState;

public interface KafkaTopicServiceOperations {
    static AdminZkClient getAdminZookeeperClient() {
        return new AdminZkClient(kafkaBrokerModuleState().getBroker().getServer().zkClient());
    }

    static Map<Object, Seq<Object>> getExistingAssignmentForTopic(String topic) {
        Set<String> topicSet = Stream.of(topic).collect(Collectors.toCollection(HashSet::new));
        scala.collection.immutable.Map<String, scala.collection.immutable.Map<Object, Seq<Object>>> partitionAssigment =
                kafkaBrokerModuleState().getBroker().getZookeeperClient().getPartitionAssignmentForTopics(JavaSetToScalaImmutableSet(topicSet));
        return partitionAssigment.get(topic).get();
    }
}
