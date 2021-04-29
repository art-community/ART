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

package io.art.rsocket.configuration;

import io.art.core.changes.*;
import io.art.core.source.*;
import io.art.rsocket.constants.RsocketModuleConstants.*;
import io.art.rsocket.exception.*;
import io.art.rsocket.refresher.*;
import lombok.*;
import reactor.netty.http.client.*;
import reactor.netty.tcp.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.combiner.SectionCombiner.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.rsocket.configuration.RsocketKeepAliveConfiguration.*;
import static io.art.rsocket.configuration.RsocketResumeConfiguration.*;
import static io.art.rsocket.configuration.RsocketRetryConfiguration.*;
import static io.art.rsocket.constants.RsocketModuleConstants.ConfigurationKeys.*;
import static io.art.rsocket.constants.RsocketModuleConstants.ExceptionMessages.*;
import static io.art.rsocket.constants.RsocketModuleConstants.PayloadDecoderMode.*;
import static io.art.rsocket.constants.RsocketModuleConstants.TransportMode.*;
import static io.art.value.constants.ValueModuleConstants.*;
import static io.art.value.constants.ValueModuleConstants.DataFormat.*;
import static io.rsocket.frame.FrameLengthCodec.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static reactor.netty.http.client.HttpClient.*;

@Getter
@RequiredArgsConstructor
public class RsocketConnectorConfiguration {
    private String connectorId;
    private PayloadDecoderMode payloadDecoderMode;
    private int maxInboundPayloadSize;
    private int fragment;
    private RsocketKeepAliveConfiguration keepAlive;
    private RsocketResumeConfiguration resume;
    private RsocketRetryConfiguration retry;
    private TransportMode transport;
    private TcpClient tcpClient;
    private int tcpMaxFrameLength;
    private HttpClient httpWebSocketClient;
    private String httpWebSocketPath;
    private boolean logging;
    private DataFormat dataFormat;
    private DataFormat metaDataFormat;

    public static RsocketConnectorConfiguration defaults() {
        RsocketConnectorConfiguration configuration = new RsocketConnectorConfiguration();
        configuration.logging = false;
        configuration.fragment = 0;
        configuration.maxInboundPayloadSize = Integer.MAX_VALUE;
        configuration.payloadDecoderMode = ZERO_COPY;
        configuration.dataFormat = JSON;
        configuration.metaDataFormat = JSON;
        configuration.tcpMaxFrameLength = FRAME_LENGTH_MASK;
        configuration.httpWebSocketPath = SLASH;
        return configuration;
    }

    public static RsocketConnectorConfiguration rsocketConnector(ConfigurationSource source) {
        RsocketConnectorConfiguration configuration = new RsocketConnectorConfiguration();
        RsocketConnectorConfiguration defaults = defaults();

        configuration.logging = orElse(source.getBool(LOGGING_KEY), defaults.logging);
        configuration.fragment = orElse(source.getInt(FRAGMENTATION_MTU_KEY), defaults.fragment);
        configuration.maxInboundPayloadSize = orElse(source.getInt(MAX_INBOUND_PAYLOAD_SIZE_KEY), defaults.maxInboundPayloadSize);
        configuration.payloadDecoderMode = rsocketPayloadDecoder(source.getString(PAYLOAD_DECODER_KEY), defaults.payloadDecoderMode);
        configuration.dataFormat = dataFormat(source.getString(DATA_FORMAT_KEY), defaults.dataFormat);
        configuration.metaDataFormat = dataFormat(source.getString(META_DATA_FORMAT_KEY), defaults.metaDataFormat);
        configuration.keepAlive = source.getNested(KEEP_ALIVE_SECTION, RsocketKeepAliveConfiguration::rsocketKeepAlive);
        configuration.resume = source.getNested(RESUME_SECTION, RsocketResumeConfiguration::rsocketResume);
        configuration.retry = source.getNested(RECONNECT_SECTION, RsocketRetryConfiguration::rsocketRetry);

        if (!source.has(TRANSPORT_SECTION)) {
            configuration.tcpMaxFrameLength = defaults.tcpMaxFrameLength;
            configuration.httpWebSocketPath = defaults.httpWebSocketPath;
            return configuration;
        }

        configuration.transport = rsocketTransport(source.getString(TRANSPORT_MODE_KEY));
        switch (configuration.transport) {
            case TCP:
                String host = source.getString(TRANSPORT_TCP_HOST_KEY);
                int port = source.getInt(TRANSPORT_TCP_PORT_KEY);
                if (isEmpty(host)) {
                    throw new RsocketException(format(CONFIGURATION_PARAMETER_NOT_EXISTS, combine(source.getSection(), TRANSPORT_TCP_HOST_KEY)));
                }
                if (isEmpty(port)) {
                    throw new RsocketException(format(CONFIGURATION_PARAMETER_NOT_EXISTS, combine(source.getSection(), TRANSPORT_PORT_KEY)));
                }
                configuration.tcpClient = TcpClient.create().port(port).host(host);
                configuration.tcpMaxFrameLength = orElse(source.getInt(TRANSPORT_TCP_MAX_FRAME_LENGTH), defaults.tcpMaxFrameLength);
                break;
            case WS:
                String url = source.getString(TRANSPORT_WS_BASE_URL_KEY);
                if (isEmpty(url)) {
                    throw new RsocketException(format(CONFIGURATION_PARAMETER_NOT_EXISTS, combine(source.getSection(), TRANSPORT_WS_BASE_URL_KEY)));
                }
                configuration.httpWebSocketClient = create().baseUrl(url);
                configuration.httpWebSocketPath = orElse(source.getString(TRANSPORT_WS_PATH_KEY), SLASH);
                break;
            default:
                throw new RsocketException(format(CONFIGURATION_PARAMETER_NOT_EXISTS, combine(source.getSection(), TRANSPORT_MODE_KEY)));
        }

        return configuration;
    }

    public static RsocketConnectorConfiguration rsocketConnector(RsocketModuleRefresher refresher, RsocketConnectorConfiguration defaults, ConfigurationSource source) {
        RsocketConnectorConfiguration configuration = new RsocketConnectorConfiguration();
        configuration.connectorId = source.getParent();

        ChangesListener listener = refresher.connectorListeners().listenerFor(configuration.connectorId);
        ChangesListener loggingListener = refresher.connectorLoggingListeners().listenerFor(configuration.connectorId);

        configuration.logging = loggingListener.emit(orElse(source.getBool(LOGGING_KEY), defaults.logging));

        configuration.dataFormat = listener.emit(dataFormat(source.getString(DATA_FORMAT_KEY), defaults.dataFormat));
        configuration.metaDataFormat = listener.emit(dataFormat(source.getString(META_DATA_FORMAT_KEY), defaults.metaDataFormat));
        configuration.payloadDecoderMode = listener.emit(rsocketPayloadDecoder(source.getString(PAYLOAD_DECODER_KEY), defaults.payloadDecoderMode));
        configuration.maxInboundPayloadSize = listener.emit(orElse(source.getInt(MAX_INBOUND_PAYLOAD_SIZE_KEY), defaults.maxInboundPayloadSize));
        configuration.fragment = listener.emit(orElse(source.getInt(FRAGMENTATION_MTU_KEY), defaults.fragment));
        configuration.keepAlive = listener.emit(let(source.getNested(KEEP_ALIVE_SECTION), section -> rsocketKeepAlive(section, defaults.keepAlive), defaults.keepAlive));
        configuration.resume = listener.emit(let(source.getNested(RESUME_SECTION), section -> rsocketResume(section, defaults.resume), defaults.resume));
        configuration.retry = listener.emit(let(source.getNested(RECONNECT_SECTION), section -> rsocketRetry(section, defaults.retry), defaults.retry));

        if (!source.has(TRANSPORT_SECTION) && isNull(defaults.transport)) {
            throw new RsocketException(format(CONFIGURATION_PARAMETER_NOT_EXISTS, combine(source.getSection(), TRANSPORT_SECTION)));
        }

        configuration.transport = listener.emit(rsocketTransport(source.getString(TRANSPORT_MODE_KEY), defaults.transport));
        switch (configuration.transport) {
            case TCP:
                String host = listener.emit(source.getString(TRANSPORT_TCP_HOST_KEY));
                Integer port = listener.emit(source.getInt(TRANSPORT_TCP_PORT_KEY));
                if (isEmpty(host) && isNull(defaults.tcpClient)) {
                    throw new RsocketException(format(CONFIGURATION_PARAMETER_NOT_EXISTS, combine(source.getSection(), TRANSPORT_TCP_HOST_KEY)));
                }
                if (isEmpty(port) && isNull(defaults.tcpClient)) {
                    throw new RsocketException(format(CONFIGURATION_PARAMETER_NOT_EXISTS, combine(source.getSection(), TRANSPORT_PORT_KEY)));
                }
                if (isEmpty(host) || isEmpty(port)) {
                    configuration.tcpClient = defaults.tcpClient;
                }
                configuration.tcpClient = orElse(configuration.tcpClient, TcpClient.create().port(port).host(host));
                configuration.tcpMaxFrameLength = listener.emit(orElse(source.getInt(TRANSPORT_TCP_MAX_FRAME_LENGTH), defaults.tcpMaxFrameLength));
                break;
            case WS:
                String url = listener.emit(source.getString(TRANSPORT_WS_BASE_URL_KEY));
                if (isEmpty(url) && isNull(defaults.httpWebSocketClient)) {
                    throw new RsocketException(format(CONFIGURATION_PARAMETER_NOT_EXISTS, combine(source.getSection(), TRANSPORT_WS_BASE_URL_KEY)));
                }
                if (isEmpty(url)) {
                    configuration.httpWebSocketClient = defaults.httpWebSocketClient;
                }
                configuration.httpWebSocketClient = orElse(configuration.httpWebSocketClient, create().baseUrl(url));
                configuration.httpWebSocketPath = listener.emit(orElse(source.getString(TRANSPORT_WS_PATH_KEY), defaults.httpWebSocketPath));
                break;
        }

        return configuration;
    }
}
