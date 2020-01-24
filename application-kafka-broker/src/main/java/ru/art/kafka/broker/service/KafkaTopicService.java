package ru.art.kafka.broker.service;

import ru.art.kafka.broker.configuration.KafkaTopicConfiguration;
public interface KafkaTopicService {
    static void addTopic(KafkaTopicConfiguration topic) {

    }

    // topic + num of partitions
    static void addPartitions() {

    }

    // topic in some request
    static void deleteTopic(String topic) {
        /*if (!getKafkaZookeeperClient().topicExists(topic)) return;
        getAdminZookeeperClient().deleteTopic(topic);*/
    }
}
