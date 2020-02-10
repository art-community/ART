package ru.art.kafka.broker.api.operations;

import ru.art.kafka.broker.api.model.KafkaTopicResult;

public interface KafkaTopicResultsCreator {
    static KafkaTopicResult createSuccessResult() {
        return KafkaTopicResult.builder()
                .result(true)
                .build();
    }
}
