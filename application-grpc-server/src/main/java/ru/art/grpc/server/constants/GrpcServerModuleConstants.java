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

package ru.art.grpc.server.constants;

import java.util.concurrent.*;

public interface GrpcServerModuleConstants {
    String GRPC_SERVER_MODULE_ID = "GRPC_SERVER_MODULE";
    String GRPC_FUNCTION_SERVICE = "GRPC_FUNCTION_SERVICE";
    String DEFAULT_MODULE_PATH = "/module";
    String GRPC_SERVICE_TYPE = "GRPC_SERVICE";
    String PROTOBUF_ERROR_MESSAGE = "ERROR_MESSAGE";
    String TRACE_ID_HEADER = "TRACE_ID";
    String PROFILE_HEADER = "PROFILE";
    String ENVIRONMENT_PROPERTY = "environment";
    String GRPC_SERVER_THREAD = "grpc-server-bootstrap-thread";
    int DEFAULT_MAX_INBOUND_MESSAGE_SIZE = 8 * 1024 * 1024;
    int DEFAULT_HANDSHAKE_TIMEOUT = 60;
    long DEFAULT_PERMIT_KEEP_ALIVE_TIME_NANOS = TimeUnit.MINUTES.toNanos(5);
}
