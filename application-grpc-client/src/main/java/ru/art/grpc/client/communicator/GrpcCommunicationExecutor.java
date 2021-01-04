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
import lombok.*;
import ru.art.entity.Value;
import ru.art.entity.*;
import ru.art.entity.interceptor.*;
import ru.art.entity.mapper.*;
import ru.art.grpc.servlet.*;
import ru.art.grpc.servlet.GrpcServlet.*;
import ru.art.service.model.*;
import static io.grpc.ManagedChannelBuilder.*;
import static java.util.Objects.*;
import static java.util.concurrent.TimeUnit.*;
import static lombok.AccessLevel.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.constants.InterceptionStrategy.*;
import static ru.art.core.extension.StringExtensions.*;
import static ru.art.entity.Value.*;
import static ru.art.grpc.client.module.GrpcClientModule.*;
import static ru.art.protobuf.descriptor.ProtobufEntityReader.*;
import static ru.art.protobuf.descriptor.ProtobufEntityWriter.*;
import static ru.art.service.factory.ServiceRequestFactory.*;
import static ru.art.service.factory.ServiceResponseFactory.*;
import static ru.art.service.mapping.ServiceRequestMapping.*;
import static ru.art.service.mapping.ServiceResponseMapping.*;
import javax.annotation.*;
import java.util.*;
import java.util.concurrent.*;

@NoArgsConstructor(access = PRIVATE)
class GrpcCommunicationExecutor {
    @SuppressWarnings("Duplicates")
    static <RequestType, ResponseType> ServiceResponse<ResponseType> execute(ManagedChannel channel, GrpcCommunicationConfiguration configuration, @Nullable RequestType request) {
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
            if (result.getNextInterceptionStrategy() == PROCESS_HANDLING) {
                break;
            }
            if (result.getNextInterceptionStrategy() == STOP_HANDLING) {
                return okResponse(command);
            }
        }
        Entity responseValue = asEntity(readProtobuf(createBlockingStub(channel, configuration).executeService(writeProtobuf(requestValue))));
        List<ValueInterceptor<Entity, Entity>> responseValueInterceptors = configuration.getResponseValueInterceptors();
        for (ValueInterceptor<Entity, Entity> responseValueInterceptor : responseValueInterceptors) {
            ValueInterceptionResult<Entity, Entity> result = responseValueInterceptor.intercept(responseValue);
            if (isNull(result)) {
                break;
            }
            responseValue = result.getOutValue();
            if (result.getNextInterceptionStrategy() == PROCESS_HANDLING) {
                break;
            }
            if (result.getNextInterceptionStrategy() == STOP_HANDLING) {
                return okResponse(command);
            }
        }
        return cast(toServiceResponse(cast(configuration.getResponseMapper())).map(responseValue));
    }

    private static GrpcServletBlockingStub createBlockingStub(ManagedChannel channel, GrpcCommunicationConfiguration configuration) {
        long deadlineTimeout = configuration.getDeadlineTimeout();
        GrpcServletBlockingStub stub = new GrpcServlet().newBlockingStub(channel, emptyIfNull(configuration.getPath()))
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
}