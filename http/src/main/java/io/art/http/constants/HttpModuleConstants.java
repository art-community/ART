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

package io.art.http.constants;

import io.art.communicator.constants.CommunicatorModuleConstants.*;
import io.art.core.exception.*;
import io.netty.handler.codec.http.*;
import java.time.*;
import lombok.*;

public interface HttpModuleConstants {
    interface ExceptionMessages {
        String SPECIFICATION_NOT_FOUND = "Specification was not found for service method identifiers: {0}";
        String CONFIGURATION_PARAMETER_NOT_EXISTS = "HTTP configuration parameter does not exists: ''{0}''";
        String NULL_AUTHENTICATION_STATUS = "Authentication status is null. Check() authentication status before apply()";
    }

    interface LoggingMessages {
        String HTTP_DISPOSING = "Disposing HTTP";
        String SERVER_STARTED = "HTTP server started";
        String SERVER_STOPPED = "HTTP server stopped";
        String COMMUNICATOR_STARTED = "HTTP communicator started\nConnector: {0}\nSetup payload: {1}";
        String COMMUNICATOR_STOPPED = "HTTP communicator stopped\nConnector: {0}\nSetup payload: {1}";

        String FIRE_AND_FORGET_REQUEST_LOG = "HTTP executing fireAndForget()\nData:\n{0}\nMetadata:\n{1}";
        String FIRE_AND_FORGET_RESPONSE_LOG = "HTTP fireAndForget() completed";
        String FIRE_AND_FORGET_EXCEPTION_LOG = "HTTP fireAndForget() failed\nException:\n{0}";

        String METADATA_PUSH_REQUEST_LOG = "HTTP executing metadataPush().Data:\n{0}\nMetadata:\n{1}";
        String METADATA_PUSH_RESPONSE_LOG = "HTTP metadataPush() completed";
        String METADATA_PUSH_EXCEPTION_LOG = "HTTP metadataPush() failed\nException:\n{0}";

        String REQUEST_RESPONSE_REQUEST_LOG = "HTTP executing requestResponse()\nData:\n{0}\nMetadata:\n{1}";
        String RESPONSE_RESPONSE_LOG = "HTTP requestResponse() completed\nData:\n{0}\nMetadata:\n{1}";
        String REQUEST_RESPONSE_EXCEPTION_LOG = "HTTP requestResponse() failed\nException:\n{0}";

        String REQUEST_STREAM_REQUEST_LOG = "HTTP executing requestStream()\nData:\n{0}\nMetadata:\n{1}";
        String REQUEST_STREAM_RESPONSE_LOG = "HTTP requestStream() processed\nData:\n{0}\nMetadata:\n{1}";
        String REQUEST_STREAM_EXCEPTION_LOG = "HTTP requestStream() failed\nException:\n{0}";

        String REQUEST_CHANNEL_REQUEST_LOG = "HTTP requestChannel() processed\nData:\n{0}\nMetadata:\n{1}";
        String REQUEST_CHANNEL_RESPONSE_LOG = "HTTP requestChannel() processed\nData:\n{0}\nMetadata:\n{1}";
        String REQUEST_CHANNEL_EXCEPTION_LOG = "HTTP requestChannel() failed\nException:\n{0}";
    }

    interface ConfigurationKeys {
        String HTTP_SECTION = "http";
        String SERVER_SECTION = "server";
        String COMMUNICATOR_SECTION = "communicator";
        String RESUME_SECTION = "produce";
        String RETRY_SECTION = "retry";
        String RECONNECT_SECTION = "reconnect";
        String KEEP_ALIVE_SECTION = "keepAlive";

        String DEFAULT_SECTION = "default";
        String DATA_FORMAT_KEY = "dataFormat";
        String SERVICE_ID_KEY = "serviceId";
        String METHOD_ID_KEY = "methodId";

        String TRANSPORT_SECTION = "transport";
        String TRANSPORT_MODE_KEY = "transport.mode";
        String TRANSPORT_PORT_KEY = "transport.port";
        String TRANSPORT_HOST_KEY = "transport.host";
        String TRANSPORT_TCP_HOST_KEY = "transport.tcp.host";
        String TRANSPORT_TCP_PORT_KEY = "transport.tcp.port";
        String TRANSPORT_WS_BASE_URL_KEY = "transport.ws.baseUrl";
        String TRANSPORT_WS_PATH_KEY = "transport.ws.path";
        String TRANSPORT_TCP_MAX_FRAME_LENGTH = "transport.tcp.maxFrameLength";

        String PAYLOAD_DECODER_KEY = "payloadDecoder";
        String LOGGING_KEY = "logging";
        String FRAGMENTATION_MTU_KEY = "fragmentationMtu";

        String POLICY_KEY = "policy";
        String BACKOFF_MAX_ATTEMPTS_KEY = "backoff.maxAttempts";
        String BACKOFF_MIN_BACKOFF_KEY = "backoff.minBackoff";
        String FIXED_DELAY_MAX_ATTEMPTS_KEY = "fixedDelay.maxAttempts";
        String FIXED_DELAY_KEY = "fixedDelay.delay";
        String MAX_KEY = "max";
        String MAX_IN_ROW_KEY = "maxInRow";
        String MAX_INBOUND_PAYLOAD_SIZE_KEY = "maxInboundPayloadSize";

        String SERVICES_KEY = "services";

        String INTERVAL_KEY = "interval";
        String MAX_LIFE_TIME_KEY = "maxLifeTime";

        String CONNECTORS_KEY = "connectors";

        String PATH_KEY = "path";
        String METHOD_KEY = "method";
    }

    interface ContextKeys {
        String REQUESTER_HTTP_KEY = "requesterHttp";
        String SETUP_PAYLOAD_KEY = "setupPayload";
    }

    interface HttpMethodNames {
        String GET_METHOD_NAME = "GET";
        String POST_METHOD_NAME = "POST";
        String PUT_METHOD_NAME = "PUT";
        String DELETE_METHOD_NAME = "DELETE";
        String OPTIONS_METHOD_NAME = "OPTIONS";
        String HEAD_METHOD_NAME = "HEAD";
        String PATCH_METHOD_NAME = "PATCH";
    }

    enum HttpMethodType {
        GET,
        POST,
        PUT,
        DELETE,
        OPTIONS,
        HEAD,
//        PATCH,
        DIRECTORY,
        FILE,
        WEBSOCKET
    }


    enum HttpRedirectCode {
        FOUND,
        SEE_OTHER,
        TEMPORARY_REDIRECT,
        MOVED_PERMANENTLY,
        PERMANENT_REDIRECT,
        MULTIPLE_CHOICES,
        NOT_MODIFIED;

        public HttpResponseStatus get() {
            switch (this){
                case FOUND:
                    return HttpResponseStatus.FOUND;
                case SEE_OTHER:
                    return HttpResponseStatus.SEE_OTHER;
                case TEMPORARY_REDIRECT:
                    return HttpResponseStatus.TEMPORARY_REDIRECT;
                case MOVED_PERMANENTLY:
                    return HttpResponseStatus.MOVED_PERMANENTLY;
                case PERMANENT_REDIRECT:
                    return HttpResponseStatus.PERMANENT_REDIRECT;
                case MULTIPLE_CHOICES:
                    return HttpResponseStatus.MULTIPLE_CHOICES;
                case NOT_MODIFIED:
                    return HttpResponseStatus.NOT_MODIFIED;
            }
            throw new ImpossibleSituationException();
        }
    }

    interface Defaults {
        long DEFAULT_RETRY_MAX_ATTEMPTS = 3;
        Duration DEFAULT_RETRY_MIN_BACKOFF = Duration.ofSeconds(1);
        Duration DEFAULT_RETRY_FIXED_DELAY = Duration.ofSeconds(1);
        int DEFAULT_RETRY_MAX = 1;
        int DEFAULT_RETRY_MAX_IN_ROW = 1;
        int DEFAULT_PORT = 80;
        Duration DEFAULT_KEEP_ALIVE_INTERVAL = Duration.ofSeconds(20);
        Duration DEFAULT_KEEP_ALIVE_MAX_LIFE_TIME = Duration.ofSeconds(90);
    }

    interface Fields {
        String SETUP_PAYLOAD_DATA_FORMAT_FIELD = "dataFormat";
        String SETUP_PAYLOAD_META_DATA_FORMAT_FIELD = "metaDataFormat";
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

        public static RetryPolicy httpRetryPolicy(String policy, RetryPolicy fallback) {
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

        public static PayloadDecoderMode httpPayloadDecoder(String decoder) {
            return httpPayloadDecoder(decoder, DEFAULT);
        }

        public static PayloadDecoderMode httpPayloadDecoder(String decoder, PayloadDecoderMode fallback) {
            if (ZERO_COPY.decoder.equalsIgnoreCase(decoder)) return ZERO_COPY;
            if (DEFAULT.decoder.equalsIgnoreCase(decoder)) return DEFAULT;
            return fallback;
        }
    }

    enum CommunicationMode {
        FIRE_AND_FORGET,
        REQUEST_RESPONSE,
        REQUEST_STREAM,
        REQUEST_CHANNEL,
        METADATA_PUSH
    }

    @Getter
    @AllArgsConstructor
    enum HttpProtocol implements CommunicatorProtocol {
        HTTP("http");

        String protocol;
    }
}
