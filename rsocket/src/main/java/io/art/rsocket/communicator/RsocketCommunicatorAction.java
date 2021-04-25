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

package io.art.rsocket.communicator;

import io.art.communicator.action.*;
import io.art.communicator.implementation.*;
import io.art.core.exception.*;
import io.art.core.factory.*;
import io.art.core.model.*;
import io.art.core.property.*;
import io.art.rsocket.configuration.*;
import io.art.rsocket.interceptor.*;
import io.art.rsocket.model.*;
import io.art.rsocket.payload.*;
import io.art.rsocket.refresher.*;
import io.art.value.immutable.Value;
import io.rsocket.*;
import io.rsocket.core.*;
import io.rsocket.plugins.*;
import io.rsocket.transport.netty.client.*;
import lombok.*;
import org.apache.logging.log4j.*;
import reactor.core.publisher.*;
import reactor.netty.http.client.*;
import reactor.netty.tcp.*;
import static io.art.communicator.module.CommunicatorModule.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.constants.CompilerSuppressingWarnings.*;
import static io.art.core.constants.MethodProcessingMode.*;
import static io.art.core.property.Property.*;
import static io.art.logging.LoggingModule.*;
import static io.art.rsocket.constants.RsocketModuleConstants.*;
import static io.art.rsocket.constants.RsocketModuleConstants.CommunicationMode.*;
import static io.art.rsocket.constants.RsocketModuleConstants.LoggingMessages.*;
import static io.art.rsocket.constants.RsocketModuleConstants.RsocketProtocol.*;
import static io.art.rsocket.manager.RsocketManager.*;
import static io.art.rsocket.module.RsocketModule.*;
import static io.art.value.mime.MimeTypeDataFormatMapper.*;
import static io.rsocket.core.RSocketClient.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static lombok.AccessLevel.*;
import java.util.function.*;

@Builder(toBuilder = true)
public class RsocketCommunicatorAction implements CommunicatorActionImplementation {
    private final NettyBufferFactory writeBufferFactory = communicatorModule().configuration().getWriteBufferFactory();

    private final CommunicatorActionIdentifier communicatorActionId;

    @Getter(lazy = true, value = PRIVATE)
    private final Logger logger = logger(RsocketCommunicatorAction.class);

    @Getter(lazy = true, value = PRIVATE)
    private final CommunicatorAction communicatorAction = communicatorAction();

    private final Property<RSocketClient> client = property(this::createClient, this::disposeClient).listenConsumer(() -> consumer()
            .connectorConsumers()
            .consumerFor(connectorConfiguration().getConnectorId()));

    private final Property<Function<Flux<Value>, Flux<Value>>> communication = property(this::communication)
            .listenProperties(client);

    private final Property<RsocketSetupPayload> setupPayload = property(this::setupPayload)
            .listenProperties(client);

    @Override
    public void initialize() {
        client.initialize();
    }

    @Override
    public void dispose() {
        client.dispose();
    }

    @Override
    public Flux<Value> communicate(Flux<Value> input) {
        return communication.get().apply(input);
    }

    private RSocketClient createClient() {
        RsocketConnectorConfiguration connectorConfiguration = connectorConfiguration();
        RSocketConnector connector = RSocketConnector.create()
                .dataMimeType(toMimeType(setupPayload.get().getDataFormat()).toString())
                .metadataMimeType(toMimeType(setupPayload.get().getMetadataFormat()).toString())
                .fragment(connectorConfiguration.getFragment())
                .interceptors(registry -> configureInterceptors(connectorConfiguration, registry));
        apply(connectorConfiguration.getKeepAlive(), keepAlive -> connector.keepAlive(keepAlive.getInterval(), keepAlive.getMaxLifeTime()));
        apply(connectorConfiguration.getResume(), resume -> connector.resume(resume.toResume()));
        apply(connectorConfiguration.getRetry(), retry -> connector.reconnect(retry.toRetry()));
        connector.setupPayload(setupPayload.get().getWriter().writePayloadMetaData(setupPayload.get().toEntity(), writeBufferFactory.newByteBuf()));
        switch (connectorConfiguration.getTransport()) {
            case TCP:
                TcpClient tcpClient = connectorConfiguration.getTcpClient();
                int tcpMaxFrameLength = connectorConfiguration.getTcpMaxFrameLength();
                Mono<RSocket> socket = connector
                        .connect(TcpClientTransport.create(tcpClient, tcpMaxFrameLength))
                        .doOnError(throwable -> getLogger().error(throwable.getMessage(), throwable));
                if (connectorConfiguration.isLogging()) {
                    socket = socket.doOnSubscribe(subscription -> getLogger().info(format(COMMUNICATOR_STARTED, connectorConfiguration.getConnectorId(), setupPayload)));
                }
                return from(socket.blockOptional().orElseThrow(ImpossibleSituationException::new));
            case WS:
                HttpClient httpWebSocketClient = connectorConfiguration.getHttpWebSocketClient();
                String httpWebSocketPath = connectorConfiguration.getHttpWebSocketPath();
                socket = connector
                        .connect(WebsocketClientTransport.create(httpWebSocketClient, httpWebSocketPath))
                        .doOnError(throwable -> getLogger().error(throwable.getMessage(), throwable));
                if (connectorConfiguration.isLogging()) {
                    socket = socket.doOnSubscribe(subscription -> getLogger().info(format(COMMUNICATOR_STARTED, connectorConfiguration.getConnectorId(), setupPayload)));
                }
                return from(socket.blockOptional().orElseThrow(ImpossibleSituationException::new));
        }
        throw new ImpossibleSituationException();
    }

    private void configureInterceptors(RsocketConnectorConfiguration connectorConfiguration, InterceptorRegistry registry) {
        String connectorId = connectorConfiguration.getConnectorId();
        registry.forResponder(new RsocketConnectorLoggingInterceptor(connectorId))
                .forRequester(new RsocketConnectorLoggingInterceptor(connectorId));
    }

    private void disposeClient(RSocketClient rsocket) {
        RsocketConnectorConfiguration connectorConfiguration = connectorConfiguration();
        disposeRsocket(rsocket);
        if (connectorConfiguration.isLogging()) {
            getLogger().info(format(COMMUNICATOR_STOPPED, connectorConfiguration.getConnectorId(), setupPayload));
        }
    }

    @SuppressWarnings(CONSTANT_CONDITIONS)
    private CommunicatorAction communicatorAction() {
        return communicatorModule()
                .configuration()
                .getRegistry()
                .findActionById(communicatorActionId)
                .orElseThrow(ImpossibleSituationException::new);
    }

    private RsocketConnectorConfiguration connectorConfiguration() {
        RsocketCommunicatorConfiguration communicatorConfiguration = rsocketModule().configuration().getCommunicatorConfiguration();
        return communicatorModule()
                .configuration()
                .findConnectorId(RSOCKET.getProtocol(), communicatorActionId)
                .map(communicatorConfiguration.getConnectorConfigurations()::get)
                .orElseGet(() -> communicatorConfiguration.getConnectorConfigurations().get(communicatorActionId.getCommunicatorId()));
    }

    private RsocketSetupPayload setupPayload() {
        RsocketConnectorConfiguration connectorConfiguration = connectorConfiguration();
        CommunicatorAction communicatorAction = getCommunicatorAction();
        return RsocketSetupPayload.builder()
                .serviceMethod(communicatorAction.getTargetServiceMethod())
                .dataFormat(connectorConfiguration.getSetupPayload().getDataFormat())
                .metadataFormat(connectorConfiguration.getSetupPayload().getMetadataFormat())
                .build();
    }

    private Function<Flux<Value>, Flux<Value>> communication() {
        RsocketPayloadWriter writer = setupPayload.get().getWriter();
        RsocketPayloadReader reader = setupPayload.get().getReader();
        RSocketClient client = this.client.get();
        switch (communicationMode()) {
            case FIRE_AND_FORGET:
                return input -> cast(client.fireAndForget(input.map(value -> writer.writePayloadData(value, writeBufferFactory.newByteBuf())).last(EMPTY_PAYLOAD)).flux());
            case REQUEST_RESPONSE:
                return input -> client
                        .requestResponse(input.map(value -> writer.writePayloadData(value, writeBufferFactory.newByteBuf())).last(EMPTY_PAYLOAD))
                        .flux()
                        .map(reader::readPayloadData)
                        .filter(data -> !data.isEmpty())
                        .map(RsocketPayloadValue::getValue);
            case REQUEST_STREAM:
                return input -> client
                        .requestStream(input.map(value -> writer.writePayloadData(value, writeBufferFactory.newByteBuf())).last(EMPTY_PAYLOAD))
                        .map(reader::readPayloadData)
                        .filter(data -> !data.isEmpty())
                        .map(RsocketPayloadValue::getValue);
            case REQUEST_CHANNEL:
                return input -> client
                        .requestChannel(input.map(value -> writer.writePayloadData(value, writeBufferFactory.newByteBuf())).switchIfEmpty(EMPTY_PAYLOAD_MONO))
                        .map(reader::readPayloadData)
                        .filter(data -> !data.isEmpty())
                        .map(RsocketPayloadValue::getValue);
            case METADATA_PUSH:
                return input -> cast(client.metadataPush(input.map(value -> writer.writePayloadMetaData(value, writeBufferFactory.newByteBuf())).last(EMPTY_PAYLOAD)).flux());
        }
        throw new ImpossibleSituationException();
    }

    private CommunicationMode communicationMode() {
        CommunicatorAction communicatorAction = getCommunicatorAction();
        if (isNull(communicatorAction.getOutputMapper())) {
            return FIRE_AND_FORGET;
        }
        if (communicatorAction.getInputMode() == FLUX) {
            return REQUEST_CHANNEL;
        }
        if (communicatorAction.getOutputMode() == FLUX) {
            return REQUEST_STREAM;
        }
        return REQUEST_RESPONSE;
    }

    private RsocketModuleRefresher.Consumer consumer() {
        return rsocketModule().configuration().getConsumer();
    }
}
