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

public interface GrpcServerLoggingMessages {
    String GRPC_STARTED_MESSAGE = "GRPC server started in {0}[ms]";
    String GRPC_RESTARTED_MESSAGE = "GRPC Server restarted in {0}[ms]";
    String GRPC_STOPPED_MESSAGE = "GRPC Server stopped in {0}[ms]";
    String GRPC_LOADED_SERVICE_MESSAGE = "GRPC service loaded: ''{0}:{1,number,#}'' - ''{2}''.''{3}''.''{4}''";
    String GRPC_ON_REQUEST_MESSAGE = "GRPC onMessage() request message:\n''{0}''";
    String GRPC_ON_RESPONSE_MESSAGE = "GRPC onMessage() response message:\n''{0}''";
    String GRPC_ON_HALF_CLOSE = "GRPC onHalfClose()";
    String GRPC_ON_CANCEL = "GRPC onCancel()";
    String GRPC_ON_CLOSE = "GRPC onClose() status: ''{0}'', metadata: ''{1}''";
    String GRPC_ON_COMPLETE = "GRPC onComplete()";
    String GRPC_ON_READY = "GRPC onReady()";
    String GRPC_LOGGING_EVENT = "grpcServletHandling";
    String GRPC_ON_REQUEST_HEADERS = "GRPC onHeaders() request headers: ''{0}''";
    String GRPC_ON_RESPONSE_HEADERS = "GRPC onHeaders() response headers: ''{0}''";
}
