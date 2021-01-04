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

package ru.art.rsocket.server;

import io.rsocket.core.*;
import io.rsocket.transport.netty.server.*;
import lombok.*;
import org.apache.logging.log4j.Logger;
import reactor.core.*;
import reactor.core.publisher.*;
import reactor.netty.http.server.*;
import reactor.netty.tcp.*;
import ru.art.rsocket.exception.*;
import ru.art.rsocket.socket.*;
import ru.art.rsocket.specification.*;
import static java.lang.System.*;
import static java.lang.Thread.*;
import static java.text.MessageFormat.*;
import static java.time.Duration.*;
import static java.util.Objects.*;
import static lombok.AccessLevel.*;
import static reactor.core.publisher.Mono.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.constants.NetworkConstants.*;
import static ru.art.core.context.Context.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.rsocket.constants.RsocketModuleConstants.ExceptionMessages.*;
import static ru.art.rsocket.constants.RsocketModuleConstants.LoggingMessages.*;
import static ru.art.rsocket.constants.RsocketModuleConstants.*;
import static ru.art.rsocket.constants.RsocketModuleConstants.RsocketTransport.*;
import static ru.art.rsocket.module.RsocketModule.*;
import static ru.art.service.ServiceModule.*;

public class RsocketServer {
    @Getter
    private final RsocketTransport transport;
    @Getter
    private final Mono<CloseableChannel> serverChannel;
    private Disposable serverDisposable;
    @Getter(lazy = true, value = PRIVATE)
    private final static Logger logger = loggingModule().getLogger(RsocketServer.class);

    private RsocketServer(RsocketTransport transport) {
        this.transport = transport;
        serverChannel = createServer();
    }

    private Mono<CloseableChannel> createServer() {
        RSocketServer server = RSocketServer.create().fragment(rsocketModule().getFragmentationMtu());
        if (rsocketModule().isResumableServer()) {
            server.resume(new Resume()
                    .sessionDuration(ofMillis(rsocketModule().getServerResumeSessionDuration()))
                    .streamTimeout(ofMillis(rsocketModule().getServerResumeStreamTimeout())));
        }
        server.interceptors(interceptorRegistry -> rsocketModule().getServerInterceptors().forEach(interceptorRegistry::forResponder));
        RSocketServer acceptor = rsocketModule()
                .getServerConfigurator()
                .apply(cast(server))
                .acceptor((setup, sendingSocket) -> just(new RsocketAcceptor(sendingSocket, setup)));
        Mono<CloseableChannel> channel;
        switch (transport) {
            case TCP:
                channel = acceptor.bind(rsocketModule().getTcpServerTransportConfigurator()
                        .apply(cast(TcpServerTransport.create(rsocketModule()
                                .getTcpServerConfigurator()
                                .apply(cast(TcpServer.create()
                                        .host(rsocketModule().getServerHost())
                                        .port(rsocketModule().getServerTcpPort())))))))
                        .onTerminateDetach();
                rsocketModuleState().setTcpServer(this);
                break;
            case WEB_SOCKET:
                channel = acceptor.bind(rsocketModule()
                        .getWebSocketServerTransportConfigurator()
                        .apply(cast(WebsocketServerTransport.create(rsocketModule()
                                .getWebSocketServerConfigurator()
                                .apply(cast(HttpServer.create()
                                        .host(rsocketModule().getServerHost())
                                        .port(rsocketModule().getServerWebSocketPort())))))))
                        .onTerminateDetach();
                rsocketModuleState().setWebSocketServer(this);
                break;
            default:
                throw new RsocketServerException(format(UNSUPPORTED_TRANSPORT, transport));
        }
        return channel
                .doOnSubscribe(subscription -> serviceModuleState()
                        .getServiceRegistry()
                        .getServices()
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().getServiceTypes().contains(RSOCKET_SERVICE_TYPE))
                        .forEach(entry -> getLogger().info(format(RSOCKET_LOADED_SERVICE_MESSAGE,
                                rsocketModule().getServerHost().equals(BROADCAST_IP_ADDRESS)
                                        ? contextConfiguration().getIpAddress()
                                        : rsocketModule().getServerHost(),
                                transport == TCP
                                        ? rsocketModule().getServerTcpPort()
                                        : rsocketModule().getServerWebSocketPort(),
                                entry.getKey(),
                                ((RsocketServiceSpecification) entry.getValue()).getRsocketService().getRsocketMethods().keySet()))));
    }

    public static RsocketServer rsocketTcpServer() {
        return new RsocketServer(TCP);
    }

    public static RsocketServer rsocketWebSocketServer() {
        return new RsocketServer(WEB_SOCKET);
    }

    public static RsocketServer startRsocketTcpServer() {
        RsocketServer rsocketServer = new RsocketServer(TCP);
        rsocketServer.subscribe();
        return rsocketServer;
    }

    public static RsocketServer startRsocketWebSocketServer() {
        RsocketServer rsocketServer = new RsocketServer(WEB_SOCKET);
        rsocketServer.subscribe();
        return rsocketServer;
    }

    public void subscribe() {
        final long timestamp = currentTimeMillis();
        serverDisposable = serverChannel.subscribe(serverChannel -> getLogger().info(format(transport == TCP
                        ? RSOCKET_TCP_ACCEPTOR_STARTED_MESSAGE
                        : RSOCKET_WS_ACCEPTOR_STARTED_MESSAGE,
                currentTimeMillis() - timestamp)));
    }

    public void await() {
        try {
            currentThread().join();
        } catch (InterruptedException throwable) {
            throw new RsocketServerException(throwable);
        }
    }

    public void restart() {
        long millis = currentTimeMillis();
        try {
            if (nonNull(serverDisposable)) {
                serverDisposable.dispose();
            }
            new RsocketServer(transport).subscribe();
            getLogger().info(format(RSOCKET_RESTARTED_MESSAGE, currentTimeMillis() - millis));
        } catch (Throwable throwable) {
            getLogger().error(RSOCKET_RESTART_FAILED);
        }
    }

    public void stop() {
        if (isNull(serverDisposable)) {
            return;
        }
        long millis = currentTimeMillis();
        try {
            serverDisposable.dispose();
            getLogger().info(format(RSOCKET_STOPPED, currentTimeMillis() - millis));
        } catch (Throwable throwable) {
            getLogger().error(RSOCKET_STOP_FAILED);
        }
    }

    public boolean isWorking() {
        return nonNull(serverDisposable);
    }
}
