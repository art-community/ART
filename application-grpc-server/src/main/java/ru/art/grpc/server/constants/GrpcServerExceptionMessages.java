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

public interface GrpcServerExceptionMessages {
    String GRPC_SERVER_INITIALIZATION_FAILED = "GRPC server initialization failed";
    String GRPC_SERVER_AWAITING_FAILED = "GRPC server awaiting failed";
    String GRPC_SERVER_RESTARTING_FAILED = "GRPC server restarting failed";
    String GRPC_SERVER_STOPPING_FAILED = "GRPC server stopping failed";
    String GRPC_SERVLET_INPUT_PARAMETERS_NULL = "GRPC Servlet request or response observer are null";
    String GRPC_SERVICE_NOT_EXISTS_MESSAGE = "GRPC Service with an id ''{0}'' not exists";
    String GRPC_METHOD_NOT_EXISTS_MESSAGE = "GRPC Method with an id ''{0}'' not exists";
    String GRPC_SERVICE_NOT_EXISTS_CODE = "GRPC_SERVICE_NOT_EXISTS";
    String GRPC_METHOD_NOT_EXISTS_CODE = "GRPC_METHOD_NOT_EXISTS";
    String GRPC_SERVICE_EXCEPTION = "GRPC server service exception";
    String GRPC_SERVLET_ERROR = "GRPC_SERVLET_ERROR";
}
