package ru.art.kafka.broker.api.model;

import lombok.Builder;
import lombok.Getter;
import ru.art.service.validation.Validatable;
import ru.art.service.validation.Validator;

import static ru.art.service.validation.ValidationExpressions.notNull;

@Getter
@Builder
public class KafkaTopicResult implements Validatable {
    private Boolean result;
    private String error;

    @Override
    public void onValidating(Validator validator) {
        validator.validate("result", result, notNull());
        if (!result) validator.validate("error", error, notNull());
    }
}
