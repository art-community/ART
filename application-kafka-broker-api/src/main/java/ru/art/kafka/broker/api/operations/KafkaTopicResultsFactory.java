package ru.art.kafka.broker.api.operations;

import ru.art.kafka.broker.api.model.KafkaTopicResult;

import static ru.art.core.constants.CharConstants.ESCAPE;
import static ru.art.core.constants.StringConstants.DOT;

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

    static <T> KafkaTopicResult<T> createErrorResponse(String message) {
        return KafkaTopicResult.<T>builder()
                .result(false)
                .error(message)
                .build();
    }

    static <T> KafkaTopicResult<T> createErrorResponse(Throwable e) {
        StringBuilder errorMessage = new StringBuilder();
        StackTraceElement[] stackTrace = e.getStackTrace();
        for(int i = 0; i < stackTrace.length; ++i) {
            StackTraceElement stackTraceElement = stackTrace[i];
            errorMessage.append(stackTraceElement.getClassName()).append(DOT).append(stackTraceElement.getMethodName()).append(DOT).append(stackTraceElement.getLineNumber()).append(ESCAPE);
        }
        return KafkaTopicResult.<T>builder()
                .result(false)
                .error(errorMessage.toString())
                .build();
    }
}
