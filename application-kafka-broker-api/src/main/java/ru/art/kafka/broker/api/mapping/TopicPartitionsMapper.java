package ru.art.kafka.broker.api.mapping;

import static ru.art.core.checker.CheckerForEmptiness.isNotEmpty;

import java.lang.String;
import ru.art.entity.Entity;
import ru.art.entity.mapper.ValueFromModelMapper;
import ru.art.entity.mapper.ValueToModelMapper;
import ru.art.kafka.broker.api.model.TopicPartitions;

public interface TopicPartitionsMapper {
	String topic = "topic";

	String numberOfPartitions = "numberOfPartitions";

	ValueToModelMapper<TopicPartitions, Entity> toTopicPartitions = entity -> isNotEmpty(entity) ? TopicPartitions.builder()
			.topic(entity.getString(topic))
			.numberOfPartitions(entity.getInt(numberOfPartitions))
			.build() : null;

	ValueFromModelMapper<TopicPartitions, Entity> fromTopicPartitions = model -> isNotEmpty(model) ? Entity.entityBuilder()
			.stringField(topic, model.getTopic())
			.intField(numberOfPartitions, model.getNumberOfPartitions())
			.build() : null;
}
