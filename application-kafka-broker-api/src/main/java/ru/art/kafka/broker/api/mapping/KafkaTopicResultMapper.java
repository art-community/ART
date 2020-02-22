package ru.art.kafka.broker.api.mapping;

import ru.art.entity.Entity;
import ru.art.entity.mapper.ValueFromModelMapper;
import ru.art.entity.mapper.ValueToModelMapper;
import ru.art.kafka.broker.api.model.KafkaTopicResult;

import java.util.List;

import static ru.art.core.checker.CheckerForEmptiness.isNotEmpty;

public class KafkaTopicResultMapper <T> {
    private String result = "result";
    private String error = "error";
    private String data = "data";

    ValueToModelMapper<KafkaTopicResult<T>, Entity> toKafkaTopicResultEmptyData = entity -> isNotEmpty(entity) ? KafkaTopicResult.<T>builder()
            .error(entity.getString(error))
            .result(entity.getBool(result))
            .build() : null;

    ValueFromModelMapper<KafkaTopicResult<T>, Entity> fromKafkaTopicResultEmptyData = model -> isNotEmpty(model) ? Entity.entityBuilder()
            .stringField(error, model.getError())
            .boolField(result, model.getResult())
            .build() : null;

    ValueToModelMapper<KafkaTopicResult<List<String>>, Entity> toKafkaTopicResultStringList = entity -> isNotEmpty(entity) ? KafkaTopicResult.<List<String>>builder()
            .error(entity.getString(error))
            .result(entity.getBool(result))
            .data(entity.findStringList(data))
            .build() : null;

//    ValueFromModelMapper<KafkaTopicResult<T>, Entity> fromKafkaTopicResultStringList = model -> isNotEmpty(model) ? Entity.entityBuilder()
//            .stringField(error, model.getError())
//            .boolField(result, model.getResult())
//            .stringCollectionField(data, )
//            .build() : null;
}
