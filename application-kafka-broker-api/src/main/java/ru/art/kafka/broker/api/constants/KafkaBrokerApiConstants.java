package ru.art.kafka.broker.api.constants;

public interface KafkaBrokerApiConstants {
    interface KafkaTopicServiceConstants {
        String KAFKA_TOPIC_SERVICE_ID = "KAFKA_TOPIC_SERVICE";

        interface Methods {
            String ADD_TOPIC = "ADD_TOPIC";
            String ADD_PARTITIONS = "ADD_PARTITIONS";
            String DELETE_TOPIC = "DELETE_TOPIC";
            String GET_ALL_TOPICS = "GET_ALL_TOPICS";
            String GET_ACTUAL_TOPICS = "GET_ACTUAL_TOPICS";
        }

        interface Paths {
            String ADD_TOPIC_PATH = "/addTopic";
            String ADD_PARTITIONS_PATH = "/addPartitions";
            String DELETE_TOPIC_PATH = "/deleteTopic";
            String GET_ALL_TOPICS_PATH = "/getAllTopics";
            String GET_ACTUAL_TOPICS_PATH = "/getActualTopics";
        }
    }
}
