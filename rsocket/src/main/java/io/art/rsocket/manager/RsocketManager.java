/*
 * ART
 *
 * Copyright 2019-2021 ART
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

package io.art.rsocket.manager;


import io.art.communicator.action.*;
import io.art.logging.logger.*;
import io.art.rsocket.configuration.*;
import io.art.rsocket.refresher.*;
import io.art.rsocket.server.*;
import lombok.*;
import reactor.core.*;
import static io.art.communicator.module.CommunicatorModule.*;
import static io.art.core.wrapper.ExceptionWrapper.*;
import static io.art.logging.module.LoggingModule.*;
import static io.art.rsocket.constants.RsocketModuleConstants.RsocketProtocol.*;
import static lombok.AccessLevel.*;

public class RsocketManager {
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = logger(RsocketManager.class);

    private final RsocketServer server;

    public RsocketManager(RsocketModuleRefresher refresher, RsocketModuleConfiguration configuration) {
        this.server = new RsocketServer(refresher, configuration);
    }

    public void initializeCommunicators() {
        communicatorModule()
                .configuration()
                .getRegistry()
                .getByProtocol(RSOCKET)
                .values()
                .forEach(proxy -> proxy.getActions().values().forEach(CommunicatorAction::initialize));
    }

    public void disposeCommunicators() {
        communicatorModule().configuration()
                .getRegistry()
                .getByProtocol(RSOCKET)
                .values()
                .forEach(proxy -> proxy.getActions().values().forEach(CommunicatorAction::dispose));
    }

    public void initializeServer() {
        server.initialize();
    }

    public void disposeServer() {
        server.dispose();
    }

    public static void disposeRsocket(Disposable rsocket) {
        if (rsocket.isDisposed()) {
            return;
        }
        ignoreException(rsocket::dispose, getLogger()::error);
    }
}
