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

package ru.art.kafka.broker.configuration;

import lombok.*;

import java.util.Map;

import static java.io.File.*;
import static java.nio.file.Paths.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.kafka.broker.constants.KafkaBrokerModuleConstants.*;

@Getter
@Builder
@EqualsAndHashCode
public class ZookeeperConfiguration {
    @Builder.Default
    private final String snapshotsDirectory = get(EMPTY_STRING).toAbsolutePath() + separator + DEFAULT_ZOOKEEPER_SNAPSHOTS_DIRECTORY;
    @Builder.Default
    private final String logsDirectory = get(EMPTY_STRING).toAbsolutePath() + separator + DEFAULT_ZOOKEEPER_LOGS_DIRECTORY;
    @Builder.Default
    private final int port = DEFAULT_ZOOKEEPER_PORT;
    @Builder.Default
    private final int maximumConnectedClients = DEFAULT_ZOOKEEPER_MAXIMUM_CONNECTED_CLIENTS;
    @Builder.Default
    private final int tickTime = DEFAULT_ZOOKEEPER_TICK_TIME;
    @Singular
    private Map<String, KafkaTopicConfiguration> kafkaDefaultTopics;
}
