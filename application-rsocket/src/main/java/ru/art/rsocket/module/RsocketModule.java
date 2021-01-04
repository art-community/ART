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

package ru.art.rsocket.module;

import io.rsocket.*;
import lombok.*;
import org.apache.logging.log4j.*;
import ru.art.core.module.Module;
import ru.art.rsocket.configuration.*;
import ru.art.rsocket.server.*;
import ru.art.rsocket.state.*;
import static lombok.AccessLevel.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.core.wrapper.ExceptionWrapper.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.rsocket.configuration.RsocketModuleConfiguration.*;
import static ru.art.rsocket.constants.RsocketModuleConstants.*;
import java.util.*;

@Getter
public class RsocketModule implements Module<RsocketModuleConfiguration, RsocketModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private static final RsocketModuleConfiguration rsocketModule = context().getModule(RSOCKET_MODULE_ID, RsocketModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private static final RsocketModuleState rsocketModuleState = context().getModuleState(RSOCKET_MODULE_ID, RsocketModule::new);
    private final String id = RSOCKET_MODULE_ID;
    private final RsocketModuleConfiguration defaultConfiguration = RsocketModuleDefaultConfiguration.DEFAULT_CONFIGURATION;
    private final RsocketModuleState state = new RsocketModuleState();
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = loggingModule().getLogger(RsocketModule.class);

    public static RsocketModuleConfiguration rsocketModule() {
        if (contextIsNotReady()) {
            return DEFAULT_CONFIGURATION;
        }
        return getRsocketModule();
    }

    public static RsocketModuleState rsocketModuleState() {
        return getRsocketModuleState();
    }

    @Override
    public void onUnload() {
        doIfNotNull(rsocketModuleState().getTcpServer(), RsocketServer::stop);
        doIfNotNull(rsocketModuleState().getWebSocketServer(), RsocketServer::stop);
        List<RSocket> clients = rsocketModuleState().getClients();
        clients.stream().filter(rsocket -> !rsocket.isDisposed()).forEach(this::disposeRsocket);
        clients.clear();
    }

    private void disposeRsocket(RSocket rsocket) {
        if (rsocket.isDisposed()) {
            return;
        }
        getLogger().info(RSOCKET_CLIENT_DISPOSING);
        ignoreException(rsocket::dispose, getLogger()::error);
    }
}
