package ru.art.kafka.broker.api.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderMethodName = "topicProperties")
public class KafkaTopicProperties {
    private Integer partitions;
    private Long retention;
    private Integer replicationFactor;
}
