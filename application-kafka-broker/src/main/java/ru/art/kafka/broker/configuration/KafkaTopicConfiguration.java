package ru.art.kafka.broker.configuration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(builderMethodName = "topicConfiguration")
public class KafkaTopicConfiguration {
    private Integer partitions;
    private Long retention;
}
