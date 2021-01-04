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

package ru.art.grpc.client.communicator;

import io.grpc.*;
import ru.art.entity.*;
import ru.art.entity.interceptor.*;
import ru.art.entity.mapper.*;
import ru.art.grpc.client.handler.*;
import ru.art.grpc.client.model.*;
import ru.art.service.model.*;
import java.util.concurrent.*;

public interface GrpcCommunicator {
    static GrpcCommunicator grpcCommunicator(GrpcCommunicationTargetConfiguration targetConfiguration) {
        return new GrpcCommunicatorImplementation(targetConfiguration);
    }

    static GrpcCommunicator grpcCommunicator(String url) {
        return new GrpcCommunicatorImplementation(url);
    }

    static GrpcCommunicator grpcCommunicator(String url, String path) {
        return new GrpcCommunicatorImplementation(url, path);
    }

    static GrpcCommunicator grpcCommunicator(String host, int port, String path) {
        return new GrpcCommunicatorImplementation(host, port, path);
    }

    GrpcCommunicator serviceId(String id);

    GrpcCommunicator methodId(String id);

    GrpcCommunicator functionId(String id);

    <RequestType> GrpcCommunicator requestMapper(ValueFromModelMapper<RequestType, ? extends Value> mapper);

    <ResponseType> GrpcCommunicator responseMapper(ValueToModelMapper<ResponseType, ? extends Value> mapper);

    GrpcCommunicator deadlineTimeout(long timeout);

    GrpcCommunicator waitForReady();

    GrpcCommunicator addInterceptor(ClientInterceptor interceptor);

    GrpcCommunicator executor(Executor executor);

    GrpcCommunicator secured();

    GrpcCommunicator keepAliveTimeNanos(long time);

    GrpcCommunicator keepAliveTimeOutNanos(long timeOut);

    GrpcCommunicator keepAliveWithoutCalls(boolean keepAliveWithoutCalls);

    void shutdownChannel();

    GrpcAsynchronousCommunicator asynchronous();

    <ResponseType> ServiceResponse<ResponseType> execute();

    <RequestType, ResponseType> ServiceResponse<ResponseType> execute(RequestType request);

    GrpcCommunicator addRequestValueInterceptor(ValueInterceptor<Entity, Entity> interceptor);

    GrpcCommunicator addResponseValueInterceptor(ValueInterceptor<Entity, Entity> interceptor);

    interface GrpcAsynchronousCommunicator {
        GrpcAsynchronousCommunicator asynchronousFuturesExecutor(Executor executor);

        <RequestType, ResponseType> GrpcAsynchronousCommunicator completionHandler(GrpcCommunicationCompletionHandler<RequestType, ResponseType> completionHandler);

        <RequestType> GrpcAsynchronousCommunicator exceptionHandler(GrpcCommunicationExceptionHandler<RequestType> errorHandler);

        <ResponseType> CompletableFuture<ServiceResponse<ResponseType>> executeAsynchronous();

        <RequestType, ResponseType> CompletableFuture<ServiceResponse<ResponseType>> executeAsynchronous(RequestType request);
    }
}
