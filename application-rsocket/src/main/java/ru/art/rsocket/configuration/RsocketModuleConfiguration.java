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

package ru.art.rsocket.configuration;

import io.rsocket.RSocketFactory.*;
import io.rsocket.core.*;
import io.rsocket.plugins.*;
import io.rsocket.transport.netty.server.*;
import lombok.*;
import reactor.netty.http.server.*;
import reactor.netty.tcp.*;
import ru.art.core.module.*;
import ru.art.entity.*;
import ru.art.entity.interceptor.*;
import ru.art.logging.*;
import ru.art.rsocket.constants.RsocketModuleConstants.*;
import ru.art.rsocket.exception.*;
import ru.art.rsocket.interceptor.*;
import ru.art.rsocket.model.*;
import static java.text.MessageFormat.*;
import static java.util.function.Function.*;
import static ru.art.core.constants.NetworkConstants.*;
import static ru.art.core.extension.ExceptionExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.core.network.selector.PortSelector.*;
import static ru.art.rsocket.constants.RsocketModuleConstants.*;
import static ru.art.rsocket.constants.RsocketModuleConstants.RsocketDataFormat.*;
import java.util.*;
import java.util.function.*;

public interface RsocketModuleConfiguration extends ModuleConfiguration {
    String getServerHost();

    int getServerTcpPort();

    int getServerWebSocketPort();

    String getBalancerHost();

    int getBalancerTcpPort();

    int getBalancerWebSocketPort();

    boolean isResumableServer();

    long getServerResumeSessionDuration();

    long getServerResumeStreamTimeout();

    boolean isResumableClient();

    long getClientResumeSessionDuration();

    long getClientResumeStreamTimeout();

    RsocketDataFormat getDataFormat();

    Map<String, RsocketCommunicationTargetConfiguration> getCommunicationTargets();

    boolean isEnableRawDataTracing();

    boolean isEnableValueTracing();

    List<RSocketInterceptor> getClientInterceptors();

    List<RSocketInterceptor> getServerInterceptors();

    default RsocketCommunicationTargetConfiguration getCommunicationTargetConfiguration(String serviceId) {
        return exceptionIfNull(getCommunicationTargets().get(serviceId),
                new RsocketClientException(format(RSOCKET_COMMUNICATION_TARGET_CONFIGURATION_NOT_FOUND, serviceId)))
                .toBuilder()
                .build();
    }

    RsocketModuleDefaultConfiguration DEFAULT_CONFIGURATION = new RsocketModuleDefaultConfiguration();

    List<ValueInterceptor<Entity, Entity>> getRequestValueInterceptors();

    List<ValueInterceptor<Entity, Entity>> getResponseValueInterceptors();

    Function<? extends HttpServer, ? extends HttpServer> getWebSocketServerConfigurator();

    Function<? extends TcpServer, ? extends TcpServer> getTcpServerConfigurator();

    Function<? extends WebsocketServerTransport, ? extends WebsocketServerTransport> getWebSocketServerTransportConfigurator();

    Function<? extends TcpServerTransport, ? extends TcpServerTransport> getTcpServerTransportConfigurator();

    Function<? extends RSocketServer, ? extends RSocketServer> getServerConfigurator();

    int getFragmentationMtu();

    @Getter
    class RsocketModuleDefaultConfiguration implements RsocketModuleConfiguration {
        private final RsocketDataFormat dataFormat = MESSAGE_PACK;
        private final String serverHost = BROADCAST_IP_ADDRESS;
        private final int serverTcpPort = findAvailableTcpPort();
        private final int serverWebSocketPort = findAvailableTcpPort();
        private final String balancerHost = LOCALHOST;
        private final int balancerTcpPort = DEFAULT_RSOCKET_TCP_PORT;
        private final int balancerWebSocketPort = DEFAULT_RSOCKET_WEB_SOCKET_PORT;
        private final boolean resumableServer = true;
        private final long serverResumeSessionDuration = DEFAULT_RSOCKET_RESUME_SESSION_DURATION;
        private final long serverResumeStreamTimeout = DEFAULT_RSOCKET_RESUME_STREAM_TIMEOUT;
        private final boolean resumableClient = true;
        private final long clientResumeSessionDuration = DEFAULT_RSOCKET_RESUME_SESSION_DURATION;
        private final long clientResumeStreamTimeout = DEFAULT_RSOCKET_RESUME_STREAM_TIMEOUT;
        private final boolean enableRawDataTracing = false;
        private final boolean enableValueTracing = false;
        private final int fragmentationMtu = 0;
        private final Function<? extends HttpServer, ? extends HttpServer> webSocketServerConfigurator = identity();
        private final Function<? extends TcpServer, ? extends TcpServer> tcpServerConfigurator = identity();
        private final Function<? extends WebsocketServerTransport, ? extends WebsocketServerTransport> webSocketServerTransportConfigurator = identity();
        private final Function<? extends TcpServerTransport, ? extends TcpServerTransport> tcpServerTransportConfigurator = identity();
        private final Function<? extends RSocketServer, ? extends RSocketServer> serverConfigurator = identity();
        @Getter(lazy = true, onMethod = @__({@SuppressWarnings("unchecked")}))
        private final List<RSocketInterceptor> serverInterceptors = initializeInterceptors();
        @Getter(lazy = true, onMethod = @__({@SuppressWarnings("unchecked")}))
        private final List<RSocketInterceptor> clientInterceptors = initializeInterceptors();
        @Getter(lazy = true, onMethod = @__({@SuppressWarnings("unchecked")}))
        private final List<ValueInterceptor<Entity, Entity>> requestValueInterceptors = initializeValueInterceptors();
        @Getter(lazy = true, onMethod = @__({@SuppressWarnings("unchecked")}))
        private final List<ValueInterceptor<Entity, Entity>> responseValueInterceptors = initializeValueInterceptors();

        private List<RSocketInterceptor> initializeInterceptors() {
            return linkedListOf(new RsocketLoggingInterceptor());
        }

        private List<ValueInterceptor<Entity, Entity>> initializeValueInterceptors() {
            return linkedListOf(new LoggingValueInterceptor<>(this::isEnableValueTracing));
        }

        private final Map<String, RsocketCommunicationTargetConfiguration> communicationTargets = mapOf();
    }
}
