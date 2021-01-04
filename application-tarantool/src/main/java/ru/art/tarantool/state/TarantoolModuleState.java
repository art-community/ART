/*
 * ART Java
 *
 * Copyright 2019 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.art.tarantool.state;

import lombok.*;
import org.tarantool.*;
import ru.art.core.module.*;
import ru.art.tarantool.configuration.*;
import ru.art.tarantool.configuration.lua.*;
import ru.art.tarantool.exception.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.tarantool.connector.TarantoolConnector.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.ExceptionMessages.*;
import static ru.art.tarantool.module.TarantoolModule.*;
import java.util.*;

@Getter
@Setter
public class TarantoolModuleState implements ModuleState {
    private final Map<String, TarantoolClient> clients = concurrentHashMap();
    private final Map<String, TarantoolClient> clusterClients = concurrentHashMap();
    private final Map<String, Set<TarantoolValueScriptConfiguration>> loadedValueScripts = concurrentHashMap();
    private final Map<String, Set<TarantoolCommonScriptConfiguration>> loadedCommonScripts = concurrentHashMap();

    @SuppressWarnings("Duplicates")
    public TarantoolClient getClient(String instanceId) {
        TarantoolClient client = clients.get(instanceId);
        if (isNull(client)) {
            client = connectToTarantoolInstance(instanceId);
            if (!client.isAlive()) {
                TarantoolConfiguration tarantoolConfiguration = getTarantoolConfiguration(instanceId, tarantoolModule().getTarantoolConfigurations());
                TarantoolConnectionConfiguration connectionConfiguration = tarantoolConfiguration.getConnectionConfiguration();
                String address = connectionConfiguration.getHost() + COLON + connectionConfiguration.getPort();
                throw new TarantoolConnectionException(format(UNABLE_TO_CONNECT_TO_TARANTOOL, instanceId, address));
            }
        }
        clients.put(instanceId, client);
        return client;
    }

    @SuppressWarnings("Duplicates")
    public TarantoolClient getClusterClient(String instanceId) {
        TarantoolClient client = clusterClients.get(instanceId);
        if (isNull(client)) {
            client = connectToTarantoolCluster(instanceId);
            if (!client.isAlive()) {
                TarantoolConfiguration tarantoolConfiguration = getTarantoolConfiguration(instanceId, tarantoolModule().getTarantoolConfigurations());
                TarantoolConnectionConfiguration connectionConfiguration = tarantoolConfiguration.getConnectionConfiguration();
                String address = connectionConfiguration.getHost() + COLON + connectionConfiguration.getPort();
                throw new TarantoolConnectionException(format(UNABLE_TO_CONNECT_TO_TARANTOOL, instanceId, address));
            }
        }
        clients.put(instanceId, client);
        return client;
    }
}
