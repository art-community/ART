package ru.art.kafka.broker.api.mapping;

import static ru.art.core.checker.CheckerForEmptiness.isNotEmpty;

import java.lang.String;
import ru.art.entity.Entity;
import ru.art.entity.mapper.ValueFromModelMapper;
import ru.art.entity.mapper.ValueToModelMapper;
import ru.art.kafka.broker.api.model.KafkaTopicProperties;

public interface KafkaTopicPropertiesMapper {
	String partitions = "partitions";

	String retentionMs = "retentionMs";

	String retentionBytes = "retentionBytes";

	String replicationFactor = "replicationFactor";

	ValueToModelMapper<KafkaTopicProperties, Entity> toKafkaTopicProperties = entity -> isNotEmpty(entity) ? KafkaTopicProperties.topicProperties()
			.partitions(entity.getInt(partitions))
			.retentionMs(entity.getLong(retentionMs))
			.retentionBytes(entity.getLong(retentionBytes))
			.replicationFactor(entity.getInt(replicationFactor))
			.build() : null;

	ValueFromModelMapper<KafkaTopicProperties, Entity> fromKafkaTopicProperties = model -> isNotEmpty(model) ? Entity.entityBuilder()
			.intField(partitions, model.getPartitions())
			.longField(retentionMs, model.getRetentionMs())
			.longField(retentionBytes, model.getRetentionBytes())
			.intField(replicationFactor, model.getReplicationFactor())
			.build() : null;
}
