package ru.art.kafka.broker.operations;

import kafka.zk.AdminZkClient;

import static ru.art.kafka.broker.module.KafkaBrokerModule.kafkaBrokerModuleState;

public interface KafkaTopicServiceOperations {
    static AdminZkClient getAdminZookeeperClient() {
        return new AdminZkClient(kafkaBrokerModuleState().getBroker().getServer().zkClient());
    }
}
