/*
 *    Copyright 2019 ART
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.art.test.specification.kafka

import ru.art.entity.Value
import ru.art.kafka.broker.api.model.KafkaTopic
import ru.art.kafka.broker.api.model.KafkaTopicProperties
import ru.art.kafka.broker.service.KafkaTopicService
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.function.BiFunction

import static ru.art.config.extensions.ConfigExtensions.configInnerMap
import static ru.art.config.extensions.activator.AgileConfigurationsActivator.useAgileConfigurations
import static ru.art.config.extensions.kafka.KafkaConfigKeys.KAFKA_TOPICS_SECTION_ID
import static ru.art.config.extensions.kafka.KafkaConfigKeys.PARTITIONS
import static ru.art.config.extensions.kafka.KafkaConfigKeys.RETENTION_MS
import static ru.art.core.constants.StringConstants.UNDERSCORE
import static ru.art.entity.PrimitivesFactory.stringPrimitive
import static ru.art.kafka.broker.embedded.EmbeddedKafkaBroker.startKafkaBroker
import static ru.art.kafka.broker.module.KafkaBrokerModule.kafkaBrokerModuleState
import static ru.art.kafka.consumer.module.KafkaConsumerModule.kafkaStreamsRegistry
import static ru.art.kafka.consumer.starter.KafkaStreamsStarter.startKafkaStreams
import static ru.art.kafka.producer.communicator.KafkaProducerCommunicator.kafkaProducerCommunicator

class KafkaSpecification extends Specification {
    def "Should startup kafka broker and process producing with streaming"() {
        setup:
        useAgileConfigurations()
        def result = ""
        startKafkaBroker()

        when:
        def latch = new CountDownLatch(1)
        kafkaProducerCommunicator("producer").pushKafkaRecord(stringPrimitive("testKey"), stringPrimitive("testValue"))
        kafkaStreamsRegistry().<Value, Value> registerStream "stream", { stream ->
            stream.peek { key, value -> result = key.toString() + UNDERSCORE + value; latch.countDown() }
        }
        startKafkaStreams()
        latch.await()

        then:
        result == "testKey_testValue"
    }

    def "Should start kafka broker and check default topics" () {
        setup:
        useAgileConfigurations()
        def result
        def kafkaDefaultTopics = configInnerMap(KAFKA_TOPICS_SECTION_ID, { key, config ->
            KafkaTopicProperties.topicProperties()
                    .partitions(config.getInt(PARTITIONS))
                    .retentionMs(config.getLong(RETENTION_MS))
                    .build()
        } as BiFunction, new HashMap<String, KafkaTopicProperties>())

        def configThemes = kafkaDefaultTopics.keySet().asList()
        when:
        startKafkaBroker()
        result = KafkaTopicService.getAllTopics()
        kafkaBrokerModuleState().getBroker().shutdown()

        then:
        System.out.println("Topics in config: " + configThemes)
        System.out.println("Topics in cluster: " + result)

        result.containsAll(configThemes)
    }

    def "Should delete one default topic" () {
        setup:
        useAgileConfigurations()
        def result
        def deletedTopic = KafkaTopic.builder()
                .topic("test1")
                .build()
        when:
        startKafkaBroker()
        KafkaTopicService.deleteTopic(deletedTopic)
        result = KafkaTopicService.getAllTopics()
        kafkaBrokerModuleState().getBroker().shutdown()
        then:
        !result.contains(deletedTopic.getTopic())
    }
}