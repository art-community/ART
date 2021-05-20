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

package io.art.http.manager;


import io.art.communicator.action.*;
import io.art.http.configuration.*;
import io.art.http.refresher.*;
import io.art.http.server.*;
import io.art.logging.logger.*;
import lombok.*;
import reactor.core.*;
import static io.art.communicator.module.CommunicatorModule.*;
import static io.art.core.wrapper.ExceptionWrapper.*;
import static io.art.logging.module.LoggingModule.*;
import static io.art.http.constants.HttpModuleConstants.HttpProtocol.*;
import static lombok.AccessLevel.*;

public class HttpManager {
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = logger(HttpManager.class);

    private final HttpServer server;

    public HttpManager(HttpModuleRefresher refresher, HttpModuleConfiguration configuration) {
        this.server = new HttpServer(refresher, configuration);
    }

    public void initializeCommunicators() {
        communicatorModule()
                .configuration()
                .getRegistry()
                .getByProtocol(HTTP)
                .values()
                .forEach(proxy -> proxy.getActions().values().forEach(CommunicatorAction::initialize));
    }

    public void disposeCommunicators() {
        communicatorModule().configuration()
                .getRegistry()
                .getByProtocol(HTTP)
                .values()
                .forEach(proxy -> proxy.getActions().values().forEach(CommunicatorAction::dispose));
    }

    public void initializeServer() {
        server.initialize();
    }

    public void disposeServer() {
        server.dispose();
    }

    public static void disposeHttp(Disposable http) {
        if (http.isDisposed()) {
            return;
        }
        ignoreException(http::dispose, getLogger()::error);
    }
}
