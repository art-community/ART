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

package io.art.http.server;

import io.art.core.property.*;
import io.art.http.configuration.*;
import io.art.http.refresher.*;
import io.art.logging.logger.*;
import io.art.server.*;
import lombok.*;
import reactor.core.*;
import reactor.core.publisher.*;
import reactor.netty.*;
import static io.art.core.property.Property.*;
import static io.art.http.constants.HttpModuleConstants.LoggingMessages.*;
import static io.art.http.manager.HttpManager.*;
import static io.art.http.router.HttpRouter.*;
import static io.art.logging.module.LoggingModule.*;
import static lombok.AccessLevel.*;

@RequiredArgsConstructor
public class HttpServer implements Server {
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = logger(HttpServer.class);

    private final HttpModuleConfiguration configuration;
    private final Property<DisposableServer> server;
    private volatile Mono<Void> closer;

    public HttpServer(HttpModuleRefresher refresher, HttpModuleConfiguration configuration) {
        this.configuration = configuration;
        server = property(this::createServer, this::disposeServer)
                .listenConsumer(refresher.consumer()::serverConsumer)
                .initialized(this::setupCloser);
    }

    @Override
    public void initialize() {
        server.initialize();
    }

    @Override
    public void dispose() {
        server.dispose();
    }

    @Override
    public boolean available() {
        return server.initialized();
    }

    private DisposableServer createServer() {
        reactor.netty.http.server.HttpServer server = configuration.getServerConfiguration().getHttpServer()
                .handle(httpRouter(configuration.getServerConfiguration()));
        Mono<? extends DisposableServer> bind = server.bind();
        if (configuration.getServerConfiguration().isLogging()) {
            bind = bind
                    .doOnSubscribe(subscription -> getLogger().info(SERVER_STARTED))
                    .doOnError(throwable -> getLogger().error(throwable.getMessage(), throwable));
        }
        return bind.block();
    }

    private void disposeServer(Disposable server) {
        disposeHttp(server);
        closer.block();
    }

    private void setupCloser(DisposableServer server) {
        this.closer = server.onDispose();
        if (configuration.getServerConfiguration().isLogging()) {
            this.closer = server.onDispose().doOnSuccess(ignore -> getLogger().info(SERVER_STOPPED));
        }
    }
}
