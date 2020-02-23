package ru.art.kafka.broker.spec;

import lombok.Getter;
import ru.art.http.client.specification.HttpCommunicationSpecification;

import static ru.art.kafka.broker.api.constants.KafkaBrokerApiConstants.KafkaTopicServiceConstants.KAFKA_TOPIC_SERVICE_ID;

@Getter
public class KafkaTopicServiceHttpCommunicationSpec implements HttpCommunicationSpecification {
    final String serviceId = KAFKA_TOPIC_SERVICE_ID;

    @Override
    public <P, R> R executeMethod(String methodId, P request) {
        return null;
    }
}
