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

package ru.art.rsocket.constants;

public interface RsocketModuleConstants {
    String RSOCKET_MODULE_ID = "RSOCKET_MODULE";
    String RSOCKET_FUNCTION_SERVICE = "RSOCKET_FUNCTION_SERVICE";
    String REQUEST_DATA = "requestData";
    String RSOCKET_SERVICE_TYPE = "RSOCKET_SERVICE";
    String RSOCKET_COMMUNICATION_SERVICE_TYPE = "RSOCKET_COMMUNICATION";
    String RSOCKET_COMMUNICATION_TARGET_CONFIGURATION_NOT_FOUND = "RSocket communication target configuration was not found for serviceId: {0}";
    String RSOCKET_CLIENT_DISPOSING = "Disposing RSocket client";
    String BINARY_MIME_TYPE = "application/binary";
    int DEFAULT_RSOCKET_TCP_PORT = 9000;
    int DEFAULT_RSOCKET_WEB_SOCKET_PORT = 10000;
    long DEFAULT_RSOCKET_RESUME_SESSION_DURATION = 24 * 60 * 60 * 1000;
    long DEFAULT_RSOCKET_RESUME_STREAM_TIMEOUT = 24 * 60 * 60 * 1000;

    enum RsocketDataFormat {
        PROTOBUF,
        JSON,
        XML,
        MESSAGE_PACK
    }

    enum RsocketInterceptingDataType {
        DATA,
        META_DATA
    }

    enum RsocketInterceptedResultAction {
        RETURN,
        PROCESS
    }

    enum RsocketTransport {
        TCP,
        WEB_SOCKET
    }

    interface ExceptionMessages {
        String SERVICE_NOT_EXISTS = "Service with id {0} does not exists in service registry";
        String METHOD_NOT_EXISTS = "Rsocket method with id {0} for service {1} does not exists in rsocketService";
        String SERVICE_NOT_SUPPORTED_RSOCKET = "Service with id {0} has not 'RSOCKET' service type";
        String UNSUPPORTED_DATA_FORMAT = "Unsupported payload data format: {0}";
        String UNSUPPORTED_TRANSPORT = "Unsupported RSocket transport: {0}";
        String RSOCKET_RESTART_FAILED = "Rsocket restart failed";
        String RSOCKET_STOP_FAILED = "Rsocket stop failed";
        String INVALID_RSOCKET_COMMUNICATION_CONFIGURATION = "Some required fields in RSocket communication configuration are null: ";
        String FAILED_TO_READ_PAYLOAD = "Payload reading failed with exception: {0}";
        String INTERCEPTING_DATA_TYPE_NULL = "Intercepting data type is null";
    }

    interface LoggingMessages {
        String RSOCKET_TCP_ACCEPTOR_STARTED_MESSAGE = "RSocket TCP acceptor started in {0}[ms]";
        String RSOCKET_WS_ACCEPTOR_STARTED_MESSAGE = "RSocket WS acceptor started in {0}[ms]";
        String RSOCKET_TCP_COMMUNICATOR_STARTED_MESSAGE = "RSocket TCP communicator to {0}:{1,number,#} started";
        String RSOCKET_WS_COMMUNICATOR_STARTED_MESSAGE = "RSocket WebSocket communicator to {0}:{1,number,#} started";
        String RSOCKET_WS_COMMUNICATOR_CREATED_MESSAGE = "RSocket WebSocket communicator to {0}:{1,number,#} created";
        String RSOCKET_TCP_COMMUNICATOR_CREATED_MESSAGE = "RSocket TCP communicator to {0}:{1,number,#} created";
        String RSOCKET_RESTARTED_MESSAGE = "RSocket Server restarted in {0}[ms]";
        String RSOCKET_STOPPED = "RSocket Server stopped in {0}[ms]";
        String RSOCKET_LOADED_SERVICE_MESSAGE = "RSocket service loaded: {0}:{1,number,#} - {2}.{3}";
        String RSOCKET_FIRE_AND_FORGET_REQUEST_LOG = "RSocket executing fireAndForget() with request data: {0} and metadata: {1}";
        String RSOCKET_FIRE_AND_FORGET_RESPONSE_LOG = "RSocket fireAndForget() completed";
        String RSOCKET_FIRE_AND_FORGET_EXCEPTION_LOG = "RSocket fireAndForget() failed with exception: {0}}";
        String RSOCKET_METADATA_PUSH_REQUEST_LOG = "RSocket executing metadataPush() with request data: {0} and metadata: {1}";
        String RSOCKET_METADATA_PUSH_RESPONSE_LOG = "RSocket metadataPush() completed";
        String RSOCKET_METADATA_PUSH_EXCEPTION_LOG = "RSocket metadataPush() failed with exception: {0}";
        String RSOCKET_REQUEST_RESPONSE_REQUEST_LOG = "RSocket executing requestResponse() with request data: {0} and metadata: {1}";
        String RSOCKET_REQUEST_RESPONSE_RESPONSE_LOG = "RSocket requestResponse() completed with response data: {0} and metadata: {1}";
        String RSOCKET_REQUEST_RESPONSE_EXCEPTION_LOG = "RSocket requestResponse() failed with exception: {0}}";
        String RSOCKET_REQUEST_STREAM_REQUEST_LOG = "RSocket executing requestStream() with request data: {0} and metadata: {1}";
        String RSOCKET_REQUEST_STREAM_RESPONSE_LOG = "RSocket requestStream() processed with response data: {0} and metadata: {1}";
        String RSOCKET_REQUEST_STREAM_EXCEPTION_LOG = "RSocket requestStream() failed with exception: {0}";
        String RSOCKET_REQUEST_CHANNEL_REQUEST_LOG = "RSocket requestChannel() processed with request data: {0} and metadata: {1}";
        String RSOCKET_REQUEST_CHANNEL_RESPONSE_LOG = "RSocket requestChannel() processed with response data: {0} and metadata: {1}";
        String RSOCKET_REQUEST_CHANNEL_EXCEPTION_LOG = "RSocket requestChannel() failed with exception: {0}";
    }
}
