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

package ru.art.tarantool.module;

import lombok.*;
import org.apache.logging.log4j.*;
import org.tarantool.*;
import ru.art.core.module.Module;
import ru.art.tarantool.configuration.*;
import ru.art.tarantool.exception.*;
import ru.art.tarantool.state.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static lombok.AccessLevel.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.wrapper.ExceptionWrapper.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.tarantool.configuration.TarantoolModuleConfiguration.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.ExceptionMessages.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.LoggingMessages.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.TarantoolInitializationMode.*;
import static ru.art.tarantool.initializer.TarantoolInitializer.*;
import java.util.*;

@Getter
public class TarantoolModule implements Module<TarantoolModuleConfiguration, TarantoolModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private final static TarantoolModuleConfiguration tarantoolModule = context().getModule(TARANTOOL_MODULE_ID, TarantoolModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private final static TarantoolModuleState tarantoolModuleState = context().getModuleState(TARANTOOL_MODULE_ID, TarantoolModule::new);
    private final String id = TARANTOOL_MODULE_ID;
    private final TarantoolModuleConfiguration defaultConfiguration = DEFAULT_CONFIGURATION;
    private final TarantoolModuleState state = new TarantoolModuleState();
    @Getter(lazy = true, value = PRIVATE)
    private final static Logger logger = loggingModule().getLogger(TarantoolModule.class);

    @Override
    public void onLoad() {
        if (tarantoolModule().getInitializationMode() != BOOTSTRAP) {
            return;
        }
        initializeTarantools();
    }

    public static TarantoolModuleConfiguration tarantoolModule() {
        if (contextIsNotReady()) {
            return DEFAULT_CONFIGURATION;
        }
        return getTarantoolModule();
    }

    public static TarantoolModuleState tarantoolModuleState() {
        return getTarantoolModuleState();
    }


    public static TarantoolConfiguration getTarantoolConfiguration(String instanceId, Map<String, TarantoolConfiguration> configurations) {
        TarantoolConfiguration configuration = configurations.get(instanceId);
        if (isNull(configuration)) {
            throw new TarantoolConnectionException(format(CONFIGURATION_IS_NULL, instanceId));
        }
        return configuration;
    }

    @Override
    public void onUnload() {
        tarantoolModuleState().getClients().entrySet().stream().filter(client -> !client.getValue().isClosed()).forEach(this::closeTarantoolClient);
        tarantoolModuleState().getClusterClients().entrySet().stream().filter(client -> !client.getValue().isClosed()).forEach(this::closeTarantoolClient);
    }

    private void closeTarantoolClient(Map.Entry<String, TarantoolClient> entry) {
        ignoreException(() -> {
            entry.getValue().close();
            getLogger().info(format(TARANTOOL_CLIENT_CLOSED, entry.getKey()));
        }, getLogger()::error);
    }
}
