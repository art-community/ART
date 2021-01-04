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

package ru.art.grpc.client.constants;

import static java.util.concurrent.TimeUnit.MINUTES;

public interface GrpcClientModuleConstants {
    String GRPC_COMMUNICATION_SERVICE_TYPE = "GRPC_COMMUNICATION";
    String GRPC_CLIENT_MODULE_ID = "GRPC_CLIENT_MODULE";
    String TRACE_ID_HEADER = "TRACE_ID";
    String PROFILE_HEADER = "PROFILE";
    long DEFAULT_GRPC_DEADLINE = 10000L;
    int DEFAULT_GRPC_PORT = 8000;
    long GRPC_CHANNEL_SHUTDOWN_TIMEOUT = 1000L;
    long IDLE_DEFAULT_TIMEOUT = MINUTES.toNanos(30L);
    String GRPC_CHANNEL_SHUTDOWN = "GRPC channel for ''{0}'' shutdown";
    String GRPC_ON_CLOSE = "GRPC onClose() status: ''{0}'', metadata: ''{1}''";
    String GRPC_ON_READY = "GRPC onReady()";
    String GRPC_ON_REQUEST_MESSAGE = "GRPC onMessage() request message:\n''{0}''";
    String GRPC_ON_RESPONSE_MESSAGE = "GRPC onMessage() response message:\n''{0}''";
    String GRPC_ON_HALF_CLOSE = "GRPC onHalfClose()";
    String GRPC_ON_CANCEL = "GRPC onCancel() message: ''{0}'', cause: ''{1}''";
    String GRPC_ON_RESPONSE_HEADERS = "GRPC onHeaders() response headers: ''{0}''";
    String GRPC_FUNCTION_SERVICE = "GRPC_FUNCTION_SERVICE";
}
