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

package io.art.rsocket.server;

import io.art.core.exception.*;
import io.art.core.property.*;
import io.art.core.runnable.*;
import io.art.logging.logger.*;
import io.art.rsocket.configuration.*;
import io.art.rsocket.interceptor.*;
import io.art.rsocket.refresher.*;
import io.art.rsocket.socket.*;
import io.art.server.*;
import io.rsocket.*;
import io.rsocket.core.*;
import io.rsocket.plugins.*;
import io.rsocket.transport.*;
import io.rsocket.transport.netty.server.*;
import lombok.*;
import reactor.core.*;
import reactor.core.publisher.*;
import reactor.netty.http.server.*;
import reactor.netty.tcp.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.property.Property.*;
import static io.art.core.wrapper.ExceptionWrapper.*;
import static io.art.logging.module.LoggingModule.*;
import static io.art.rsocket.constants.RsocketModuleConstants.LoggingMessages.*;
import static io.art.rsocket.constants.RsocketModuleConstants.*;
import static io.art.rsocket.constants.RsocketModuleConstants.TransportMode.*;
import static io.art.rsocket.manager.RsocketManager.*;
import static java.text.MessageFormat.*;
import static java.util.Optional.*;
import static lombok.AccessLevel.*;
import java.net.*;
import java.util.*;
import java.util.function.*;

@RequiredArgsConstructor
public class RsocketServer implements Server {
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = logger(RsocketServer.class);

    private final RsocketModuleConfiguration configuration;
    private final Property<CloseableChannel> channel;
    private volatile Mono<Void> closer;

    public RsocketServer(RsocketModuleRefresher refresher, RsocketModuleConfiguration configuration) {
        this.configuration = configuration;
        channel = property(this::createServer, this::disposeServer)
                .listenConsumer(refresher.consumer()::serverConsumer)
                .initialized(this::setupCloser);
    }

    @Override
    public void initialize() {
        channel.initialize();
    }

    @Override
    public void dispose() {
        channel.dispose();
    }

    @Override
    public boolean available() {
        return channel.initialized();
    }

    private CloseableChannel createServer() {
        RsocketServerConfiguration configuration = this.configuration.getServerConfiguration();
        TransportMode transportMode = configuration.getTransport();
        int fragmentationMtu = configuration.getFragmentationMtu();
        RSocketServer server = RSocketServer.create(this::createAcceptor)
                .maxInboundPayloadSize(configuration.getMaxInboundPayloadSize());
        if (fragmentationMtu > 0) {
            server.fragment(fragmentationMtu);
        }
        apply(configuration.getResume(), resume -> server.resume(resume.toResume()));
        Optional<SocketAddress> tcpAddress = ofNullable(configuration.getTcpServer()).map(TcpServer::configuration).map(TcpServerConfig::bindAddress).map(Supplier::get);
        Optional<SocketAddress> webAddress = ofNullable(configuration.getHttpWebSocketServer()).map(HttpServer::configuration).map(HttpServerConfig::bindAddress).map(Supplier::get);
        SocketAddress address = transportMode == TCP ? tcpAddress.orElseThrow(ImpossibleSituationException::new) : webAddress.orElseThrow(ImpossibleSituationException::new);
        ServerTransport<CloseableChannel> transport = transportMode == TCP
                ? TcpServerTransport.create(configuration.getTcpServer(), configuration.getTcpMaxFrameLength())
                : WebsocketServerTransport.create(configuration.getHttpWebSocketServer());
        Mono<CloseableChannel> bind = server
                .interceptors(this::configureInterceptors)
                .payloadDecoder(configuration.getPayloadDecoder())
                .bind(transport);
        if (configuration.isLogging()) {
            bind = bind
                    .doOnSubscribe(subscription -> getLogger().info(format(SERVER_STARTED, address)))
                    .doOnError(throwable -> getLogger().error(throwable.getMessage(), throwable));
        }
        return bind.block();
    }

    private void configureInterceptors(InterceptorRegistry registry) {
        registry.forResponder(new RsocketServerLoggingInterceptor()).forRequester(new RsocketServerLoggingInterceptor());
    }

    private void disposeServer(Disposable server) {
        disposeRsocket(server);
        closer.block();
    }

    private Mono<RSocket> createAcceptor(ConnectionSetupPayload payload, RSocket requesterSocket) {
        Mono<RSocket> socket = Mono.create(emitter -> createSocket(payload, requesterSocket, emitter));
        return socket.doOnError(throwable -> getLogger().error(throwable.getMessage(), throwable));
    }

    private void createSocket(ConnectionSetupPayload payload, RSocket requester, MonoSink<RSocket> emitter) {
        ExceptionRunnable createRsocket = () -> emitter.success(new ServingRsocket(payload, requester, configuration.getServerConfiguration()));
        ignoreException(createRsocket, throwable -> getLogger().error(throwable.getMessage(), throwable));
    }

    private void setupCloser(CloseableChannel channel) {
        this.closer = channel.onClose();
        if (configuration.getServerConfiguration().isLogging()) {
            this.closer = channel.onClose().doOnSuccess(ignore -> getLogger().info(SERVER_STOPPED));
        }
    }
}
