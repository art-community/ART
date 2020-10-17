/*
 * ART
 *
 * Copyright 2020 ART
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

package io.art.rsocket.constants;

import io.art.rsocket.exception.*;
import lombok.*;
import static io.art.rsocket.constants.RsocketModuleConstants.ExceptionMessages.*;
import static java.text.MessageFormat.*;
import java.time.*;

public interface RsocketModuleConstants {
    String RSOCKET_REQUESTER_DISPOSE = "Disposing RSocket request";

    interface ExceptionMessages {
        String SPECIFICATION_NOT_FOUND = "Setup payload was null or not contained serviceId, methodId. Default service method id was not specified in configuration";
        String UNSUPPORTED_RETRY_POLICY = "Retry policy not support: ''{0}''";
        String UNSUPPORTED_DATA_FORMAT = "Unsupported payload data format: ''{0}''";
    }

    interface LoggingMessages {
        String TCP_SERVER_STARTED_MESSAGE = "RSocket TCP server started";
        String WS_SERVER_STARTED_MESSAGE = "RSocket WS server started";
        String TCP_COMMUNICATOR_STARTED_MESSAGE = "RSocket TCP communicator to {0}:{1,number,#} started";
        String WS_COMMUNICATOR_STARTED_MESSAGE = "RSocket WebSocket communicator to {0}:{1,number,#} started";
        String SERVER_STOPPED = "RSocket Server stopped";
        String FIRE_AND_FORGET_REQUEST_LOG = "RSocket executing fireAndForget() with request data: {0} and metadata: {1}";
        String FIRE_AND_FORGET_RESPONSE_LOG = "RSocket fireAndForget() completed";
        String FIRE_AND_FORGET_EXCEPTION_LOG = "RSocket fireAndForget() failed with exception: {0}";
        String METADATA_PUSH_REQUEST_LOG = "RSocket executing metadataPush() with request data: {0} and metadata: {1}";
        String METADATA_PUSH_RESPONSE_LOG = "RSocket metadataPush() completed";
        String METADATA_PUSH_EXCEPTION_LOG = "RSocket metadataPush() failed with exception: {0}";
        String REQUEST_RESPONSE_REQUEST_LOG = "RSocket executing requestResponse() with request data: {0} and metadata: {1}";
        String RESPONSE_RESPONSE_LOG = "RSocket requestResponse() completed with response data: {0} and metadata: {1}";
        String REQUEST_RESPONSE_EXCEPTION_LOG = "RSocket requestResponse() failed with exception: {0}";
        String REQUEST_STREAM_REQUEST_LOG = "RSocket executing requestStream() with request data: {0} and metadata: {1}";
        String REQUEST_STREAM_RESPONSE_LOG = "RSocket requestStream() processed with response data: {0} and metadata: {1}";
        String REQUEST_STREAM_EXCEPTION_LOG = "RSocket requestStream() failed with exception: {0}";
        String REQUEST_CHANNEL_REQUEST_LOG = "RSocket requestChannel() processed with request data: {0} and metadata: {1}";
        String REQUEST_CHANNEL_RESPONSE_LOG = "RSocket requestChannel() processed with response data: {0} and metadata: {1}";
        String REQUEST_CHANNEL_EXCEPTION_LOG = "RSocket requestChannel() failed with exception: {0}";
    }

    interface ConfigurationKeys {
        String SERVER_SECTION = "rsocket.server";
        String COMMUNICATOR_SECTION = "rsocket.communicator";
        String RESUME_SECTION = "resume";
        String RETRY_SECTION = "retry";
        String RECONNECT_SECTION = "reconnect";
        String KEEP_ALIVE_SECTION = "keepAlive";

        String TRANSPORT_MODE_KEY = "transport.mode";
        String TRANSPORT_PORT_KEY = "transport.host";
        String TRANSPORT_HOST_KEY = "transport.port";
        String PAYLOAD_DECODER_KEY = "payloadDecoder";
        String TRACING_KEY = "tracing";
        String DEFAULT_DATA_FORMAT_KEY = "defaultDataFormat";
        String DEFAULT_META_DATA_FORMAT_KEY = "defaultMetaDataFormat";
        String FRAGMENTATION_MTU_KEY = "fragmentationMtu";

        String CLEANUP_STORE_ON_KEEP_ALIVE_KEY = "cleanupStoreOnKeepAlive";
        String SESSION_DURATION_KEY = "sessionDuration";
        String STREAM_TIMEOUT_KEY = "streamTimeout";
        String POLICY_KEY = "policy";
        String BACKOFF_MAX_ATTEMPTS_KEY = "backoff.maxAttempts";
        String BACKOFF_MIN_BACKOFF_KEY = "backoff.minBackoff";
        String FIXED_DELAY_MAX_ATTEMPTS_KEY = "fixedDelay.maxAttempts";
        String FIXED_DELAY_KEY = "fixedDelay.delay";
        String MAX_KEY = "max";
        String MAX_IN_ROW_KEY = "maxInRow";
        String MAX_INBOUND_PAYLOAD_SIZE_KEY = "maxInboundPayloadSize";

        String SERVICES_KEY = "services";
        String DEFAULT_SERVICE_ID_KEY = "services.default.serviceId";
        String DEFAULT_METHOD_ID_KEY = "services.default.methodId";
        String TRANSPORT_TCP_MAX_FRAME_LENGTH = "transport.tcp.maxFrameLength";

        String INTERVAL_KEY = "interval";
        String MAX_LIFE_TIME_KEY = "maxLifeTime";

        String CONNECTORS_KEY = "connectors";
    }

    interface ContextKeys {
        String REQUESTER_RSOCKET_KEY = "requesterRsocket";
    }

    interface Defaults {
        long DEFAULT_RETRY_MAX_ATTEMPTS = 3;
        Duration DEFAULT_RETRY_MIN_BACKOFF = Duration.ofSeconds(1);
        Duration DEFAULT_RETRY_FIXED_DELAY = Duration.ofSeconds(1);
        int DEFAULT_RETRY_MAX = 1;
        int DEFAULT_RETRY_MAX_IN_ROW = 1;
        int DEFAULT_PORT = 9000;
        Duration DEFAULT_RESUME_SESSION_DURATION = Duration.ofHours(1);
        Duration DEFAULT_RESUME_STREAM_TIMEOUT = Duration.ofHours(1);
        Duration DEFAULT_KEEP_ALIVE_INTERVAL = Duration.ofSeconds(20);
        Duration DEFAULT_KEEP_ALIVE_MAX_LIFE_TIME = Duration.ofSeconds(90);
    }

    interface Fields {
        String SETUP_PAYLOAD_DATA_FORMAT_FIELD = "dataFormat";
        String SETUP_PAYLOAD_META_DATA_FORMAT_FIELD = "metaDataFormat";
        String SETUP_PAYLOAD_SERVICE_ID_FIELD = "serviceId";
        String SETUP_PAYLOAD_METHOD_ID_FIELD = "methodId";
    }


    @Getter
    @AllArgsConstructor
    enum TransportMode {
        TCP("tcp"),
        WEB_SOCKET("webSocket");

        private final String transport;

        public static TransportMode rsocketTransport(String transport) {
            if (TCP.transport.equalsIgnoreCase(transport)) return TCP;
            if (WEB_SOCKET.transport.equalsIgnoreCase(transport)) return WEB_SOCKET;
            return TCP;
        }
    }

    @Getter
    @AllArgsConstructor
    enum RetryPolicy {
        BACKOFF("backoff"),
        FIXED_DELAY("fixedDelay"),
        MAX("max"),
        MAX_IN_A_ROW("maxInARow"),
        INDEFINITELY("indefinitely");

        private final String policy;

        public static RetryPolicy rsocketRetryPolicy(String policy, RetryPolicy fallback) {
            if (BACKOFF.policy.equalsIgnoreCase(policy)) return BACKOFF;
            if (FIXED_DELAY.policy.equalsIgnoreCase(policy)) return FIXED_DELAY;
            if (MAX.policy.equalsIgnoreCase(policy)) return MAX;
            if (MAX_IN_A_ROW.policy.equalsIgnoreCase(policy)) return MAX_IN_A_ROW;
            if (INDEFINITELY.policy.equalsIgnoreCase(policy)) return INDEFINITELY;
            return fallback;
        }
    }

    @Getter
    @AllArgsConstructor
    enum PayloadDecoderMode {
        ZERO_COPY("zeroCopy"),
        DEFAULT("default");

        private final String decoder;

        public static PayloadDecoderMode rsocketPayloadDecoder(String decoder) {
            if (ZERO_COPY.decoder.equalsIgnoreCase(decoder)) return ZERO_COPY;
            return DEFAULT;
        }
    }

    enum CommunicationMode {
        FIRE_AND_FORGET,
        REQUEST_RESPONSE,
        REQUEST_STREAM,
        REQUEST_CHANNEL,
        METADATA_PUSH;
    }
}
