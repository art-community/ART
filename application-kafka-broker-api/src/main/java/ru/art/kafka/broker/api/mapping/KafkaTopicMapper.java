package ru.art.kafka.broker.api.mapping;

import static ru.art.core.checker.CheckerForEmptiness.isNotEmpty;

import java.lang.String;
import ru.art.entity.Entity;
import ru.art.entity.mapper.ValueFromModelMapper;
import ru.art.entity.mapper.ValueToModelMapper;
import ru.art.kafka.broker.api.model.KafkaTopic;

public interface KafkaTopicMapper {
	String topic = "topic";

	String properties = "properties";

	ValueToModelMapper<KafkaTopic, Entity> toKafkaTopic = entity -> isNotEmpty(entity) ? KafkaTopic.builder()
			.topic(entity.getString(topic))
			.properties(entity.getValue(properties, KafkaTopicPropertiesMapper.toKafkaTopicProperties))
			.build() : null;

	ValueFromModelMapper<KafkaTopic, Entity> fromKafkaTopic = model -> isNotEmpty(model) ? Entity.entityBuilder()
			.stringField(topic, model.getTopic())
			.entityField(properties, model.getProperties(), KafkaTopicPropertiesMapper.fromKafkaTopicProperties)
			.build() : null;
}
