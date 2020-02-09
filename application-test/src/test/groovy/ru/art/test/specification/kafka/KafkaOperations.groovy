package ru.art.test.specification.kafka

import ru.art.kafka.broker.api.model.KafkaTopicProperties

import java.util.function.BiFunction

import static ru.art.config.extensions.ConfigExtensions.configInnerMap
import static ru.art.config.extensions.kafka.KafkaConfigKeys.KAFKA_TOPICS_SECTION_ID
import static ru.art.config.extensions.kafka.KafkaConfigKeys.PARTITIONS
import static ru.art.config.extensions.kafka.KafkaConfigKeys.RETENTION_MS

class KafkaOperations {
    static Map<String, KafkaTopicProperties> getDefaultTopicsFromConfig() {
        return configInnerMap(KAFKA_TOPICS_SECTION_ID, { key, config ->
            KafkaTopicProperties.topicProperties()
                    .partitions(config.getInt(PARTITIONS))
                    .retentionMs(config.getLong(RETENTION_MS))
                    .build()
        } as BiFunction, new HashMap<String, KafkaTopicProperties>())
    }
}
