package ru.art.kafka.broker.service;

import ru.art.kafka.broker.configuration.KafkaTopicConfiguration;

public interface KafkaTopicService {
    static void addTopic(KafkaTopicConfiguration topic) {

    }

    // topic + num of partitions
    static void addPartitions() {

    }

    // topic
    static void deleteTopic(String topic) {

    }
}
