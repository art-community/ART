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

package ru.art.kafka.broker.embedded;/*
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;
import ru.art.kafka.broker.configuration.ZookeeperConfiguration;
import ru.art.kafka.broker.exception.KafkaBrokerModuleException;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Paths.get;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.zookeeper.server.NIOServerCnxnFactory.createFactory;
import static ru.art.kafka.broker.constants.KafkaBrokerModuleConstants.ZOOKEEPER_PREFIX;
import static ru.art.kafka.broker.module.KafkaBrokerModule.kafkaBrokerModule;
import static ru.art.kafka.broker.module.KafkaBrokerModule.kafkaBrokerModuleState;
import java.nio.file.Path;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class EmbeddedZookeeper {
    private final ServerCnxnFactory connectionFactory;
    private final ZookeeperConfiguration configuration;

    public static EmbeddedZookeeper startZookeeper(ZookeeperConfiguration configuration) {
        try {
            ServerCnxnFactory factory = createFactory(configuration.getPort(), configuration.getMaximumConnectedClients());
            Path snapshotsPath = get(configuration.getSnapshotsDirectory());
            createDirectories(snapshotsPath);
            snapshotsPath = createTempDirectory(snapshotsPath, ZOOKEEPER_PREFIX);
            Path logsPath = get(configuration.getLogsDirectory());
            createDirectories(logsPath);
            EmbeddedZookeeper zookeeper = new EmbeddedZookeeper(factory, configuration);
            kafkaBrokerModuleState().setZookeeper(zookeeper);
            factory.startup(new ZooKeeperServer(snapshotsPath.toFile(), logsPath.toFile(), configuration.getTickTime()));
            return zookeeper;
        } catch (Throwable e) {
            throw new KafkaBrokerModuleException(e);
        }
    }

    public static void startZookeeper() {
        startZookeeper(kafkaBrokerModule().getZookeeperConfiguration());
    }

    public void shutdown() {
        connectionFactory.shutdown();
    }

    public void restart() {
        shutdown();
        startZookeeper(configuration);
    }
}
