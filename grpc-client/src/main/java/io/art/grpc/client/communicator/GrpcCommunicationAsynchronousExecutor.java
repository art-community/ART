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

package io.art.grpc.client.communicator;

import com.google.common.util.concurrent.*;
import io.art.value.immutable.Value;
import io.grpc.*;
import lombok.*;
import io.art.grpc.client.exception.*;
import io.art.grpc.servlet.*;
import io.art.grpc.servlet.GrpcServlet.*;
import static com.google.common.util.concurrent.Futures.*;
import static java.util.Objects.*;
import static java.util.Optional.*;
import static java.util.concurrent.CompletableFuture.*;
import static java.util.concurrent.TimeUnit.*;
import static lombok.AccessLevel.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.constants.InterceptionStrategy.*;
import static io.art.core.extensions.StringExtensions.*;
import static io.art.value.immutable.Value.*;
import static io.art.grpc.client.constants.GrpcClientExceptionMessages.*;
import static io.art.grpc.client.module.GrpcClientModule.*;
import static io.art.protobuf.descriptor.ProtobufReader.*;
import static io.art.protobuf.descriptor.ProtobufWriter.*;
import static io.art.service.factory.ServiceRequestFactory.*;
import static io.art.service.factory.ServiceResponseFactory.*;
import static io.art.service.mapping.ServiceRequestMapping.*;
import static io.art.service.mapping.ServiceResponseMapping.*;
import javax.annotation.*;
import java.util.*;
import java.util.concurrent.*;

@NoArgsConstructor(access = PRIVATE)
class GrpcCommunicationAsynchronousExecutor {

    @SuppressWarnings("Duplicates")
    static <RequestType, ResponseType> CompletableFuture<ServiceResponse<ResponseType>> execute(ManagedChannel channel,
                                                                                                GrpcCommunicationConfiguration configuration,
                                                                                                @Nullable RequestType request) {
        ServiceMethodCommand command = new ServiceMethodCommand(configuration.getServiceId(), configuration.getMethodId());
        ValueFromModelMapper<?, ? extends Value> requestMapper = null;
        ServiceRequest<Object> serviceRequest = isNull(request)
                || isNull(requestMapper = configuration.getRequestMapper())
                ? newServiceRequest(command)
                : newServiceRequest(command, request);
        Entity requestValue = fromServiceRequest(cast(requestMapper)).map(serviceRequest);
        List<ValueInterceptor<Entity, Entity>> requestValueInterceptors = configuration.getRequestValueInterceptors();
        for (ValueInterceptor<Entity, Entity> requestValueInterceptor : requestValueInterceptors) {
            ValueInterceptionResult<Entity, Entity> result = requestValueInterceptor.intercept(requestValue);
            if (isNull(result)) {
                break;
            }
            requestValue = result.getOutValue();
            if (result.getNextInterceptionStrategy() == PROCESS) {
                break;
            }
            if (result.getNextInterceptionStrategy() == TERMINATE) {
                return completedFuture(okResponse(command));
            }
        }
        ListenableFuture<com.google.protobuf.Value> future = createFutureStub(channel, configuration).executeService(writeProtobuf(requestValue));
        CompletableFuture<ServiceResponse<?>> completableFuture = new CompletableFuture<>();
        addCallback(future, createFutureCallback(configuration, request, completableFuture), configuration.getAsynchronousFuturesExecutor());
        return cast(completableFuture);
    }

    private static GrpcServletFutureStub createFutureStub(ManagedChannel channel, GrpcCommunicationConfiguration configuration) {
        long deadlineTimeout = configuration.getDeadlineTimeout();
        GrpcServlet.GrpcServletFutureStub stub = new GrpcServlet().newFutureStub(channel, emptyIfNull(configuration.getPath()))
                .withDeadlineAfter(deadlineTimeout > 0L ? deadlineTimeout : grpcClientModule().getTimeout(), MILLISECONDS)
                .withInterceptors(configuration.getInterceptors().toArray(new ClientInterceptor[0]));
        if (configuration.isWaitForReady()) {
            stub = stub.withWaitForReady();
        }
        Executor executor;
        if (nonNull(executor = configuration.getOverrideExecutor()) || nonNull(executor = grpcClientModule().getOverridingExecutor())) {
            stub = stub.withExecutor(executor);
        }
        return stub;
    }

    private static FutureCallback<com.google.protobuf.Value> createFutureCallback(GrpcCommunicationConfiguration configuration,
                                                                                  Object request,
                                                                                  CompletableFuture<ServiceResponse<?>> completableFuture) {
        return new FutureCallback<com.google.protobuf.Value>() {
            @Override
            public void onSuccess(com.google.protobuf.Value response) {
                if (isNull(response)) {
                    if (isNull(configuration.getExceptionHandler())) {
                        completableFuture.completeExceptionally(new GrpcClientException(RESPONSE_IS_NULL));
                        return;
                    }
                    configuration.getExceptionHandler().failed(empty(), new GrpcClientException(RESPONSE_IS_NULL));
                    completableFuture.completeExceptionally(new GrpcClientException(RESPONSE_IS_NULL));
                    return;
                }
                Entity responseValue = asEntity(readProtobuf(response));
                List<ValueInterceptor<Entity, Entity>> responseValueInterceptors = configuration.getResponseValueInterceptors();
                for (ValueInterceptor<Entity, Entity> responseValueInterceptor : responseValueInterceptors) {
                    ValueInterceptionResult<Entity, Entity> result = responseValueInterceptor.intercept(responseValue);
                    if (isNull(result)) {
                        break;
                    }
                    responseValue = result.getOutValue();
                    if (result.getNextInterceptionStrategy() == PROCESS) {
                        break;
                    }
                    if (result.getNextInterceptionStrategy() == TERMINATE) {
                        completableFuture.complete(okResponse(new ServiceMethodCommand(configuration.getServiceId(), configuration.getMethodId())));
                        return;
                    }
                }
                ServiceResponse<?> serviceResponse = toServiceResponse(cast(configuration.getResponseMapper())).map(responseValue);
                if (isNull(configuration.getCompletionHandler())) {
                    completableFuture.complete(serviceResponse);
                    return;
                }
                configuration.getCompletionHandler().onComplete(ofNullable(cast(request)), cast(response));
                completableFuture.complete(serviceResponse);
            }

            @Override
            @ParametersAreNonnullByDefault
            public void onFailure(Throwable exception) {
                if (isNull(configuration.getExceptionHandler())) {
                    return;
                }
                configuration.getExceptionHandler().failed(ofNullable(cast(request)), exception);
                completableFuture.completeExceptionally(exception);
            }
        };
    }
}