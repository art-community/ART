package ru.art.kafka.broker.api.operations;

import ru.art.kafka.broker.api.model.KafkaTopicResult;

public interface KafkaTopicResultsFactory {
    static <T> KafkaTopicResult<T> createOkEmptyResponse() {
        return KafkaTopicResult.<T>builder()
                .result(true)
                .build();
    }

    static <T> KafkaTopicResult<T> createOkResponse(T data) {
        return KafkaTopicResult.<T>builder()
                .result(true)
                .data(data)
                .build();
    }
}
