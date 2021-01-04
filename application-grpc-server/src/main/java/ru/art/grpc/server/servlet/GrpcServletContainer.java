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

package ru.art.grpc.server.servlet;

import io.grpc.stub.*;
import lombok.*;
import ru.art.entity.*;
import ru.art.entity.interceptor.*;
import ru.art.entity.mapper.ValueToModelMapper.*;
import ru.art.grpc.server.exception.*;
import ru.art.grpc.server.model.GrpcService.*;
import ru.art.grpc.server.specification.*;
import ru.art.grpc.servlet.*;
import ru.art.logging.*;
import ru.art.service.exception.*;
import ru.art.service.model.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static org.apache.logging.log4j.ThreadContext.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.isEmpty;
import static ru.art.core.constants.InterceptionStrategy.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.entity.Value.*;
import static ru.art.entity.mapper.ValueFromModelMapper.*;
import static ru.art.grpc.server.constants.GrpcServerExceptionMessages.*;
import static ru.art.grpc.server.constants.GrpcServerLoggingMessages.*;
import static ru.art.grpc.server.constants.GrpcServerModuleConstants.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.logging.LoggingModuleConstants.*;
import static ru.art.logging.LoggingModuleConstants.LoggingParameters.*;
import static ru.art.logging.LoggingParametersManager.*;
import static ru.art.protobuf.descriptor.ProtobufEntityReader.*;
import static ru.art.protobuf.descriptor.ProtobufEntityWriter.*;
import static ru.art.service.ServiceController.*;
import static ru.art.service.ServiceModule.serviceModuleState;
import static ru.art.service.constants.RequestValidationPolicy.*;
import static ru.art.service.factory.ServiceRequestFactory.*;
import static ru.art.service.factory.ServiceResponseFactory.*;
import static ru.art.service.mapping.ServiceRequestMapping.*;
import static ru.art.service.mapping.ServiceResponseMapping.*;
import java.util.*;

public class GrpcServletContainer extends GrpcServlet {
    @Getter
    private final GrpcServletImplementation servlet;

    public GrpcServletContainer(String path, Map<String, GrpcServiceSpecification> services) {
        servlet = new GrpcServletImplementation(path, services);
    }

    public class GrpcServletImplementation extends GrpcServletImplBase {
        private final Map<String, GrpcServiceSpecification> services;

        private GrpcServletImplementation(String path, Map<String, GrpcServiceSpecification> services) {
            super(path);
            this.services = services;
        }

        @Override
        public void executeService(com.google.protobuf.Value grpcRequest, StreamObserver<com.google.protobuf.Value> responseObserver) {
            ServiceMethodCommand command = toServiceRequest()
                    .map(asEntity(readProtobuf(grpcRequest)))
                    .getServiceMethodCommand();
            String serviceId = command.getServiceId();
            String serviceMethodId = serviceId + DOT + command.getMethodId() + BRACKETS;
            clearServiceCallLoggingParameters();
            putServiceCallLoggingParameters(ServiceCallLoggingParameters.builder()
                    .serviceId(serviceId)
                    .serviceMethodId(serviceMethodId)
                    .serviceMethodCommand(serviceMethodId + DOT + getOrElse(get(REQUEST_ID_KEY), DEFAULT_REQUEST_ID))
                    .logEventType(GRPC_LOGGING_EVENT)
                    .loadedServices(serviceModuleState().getServiceRegistry().getServices().keySet())
                    .build());
            try {
                executeServiceChecked(grpcRequest, command, responseObserver);
                clearServiceCallLoggingParameters();
            } catch (Throwable throwable) {
                loggingModule()
                        .getLogger(GrpcServletContainer.class)
                        .error(GRPC_SERVICE_EXCEPTION, throwable);
                responseObserver.onNext(writeProtobuf(fromServiceResponse().map(errorResponse(command, GRPC_SERVLET_ERROR, throwable))));
                responseObserver.onCompleted();
                clearServiceCallLoggingParameters();
            }
        }

        private void executeServiceChecked(com.google.protobuf.Value grpcRequest, ServiceMethodCommand command, StreamObserver<com.google.protobuf.Value> responseObserver) {
            if (isNull(grpcRequest) || isNull(responseObserver)) {
                throw new GrpcServletException(GRPC_SERVLET_INPUT_PARAMETERS_NULL);
            }
            Entity serviceRequestEntity = asEntity(readProtobuf(grpcRequest));
            String serviceId = command.getServiceId();
            String serviceMethodId = command.getMethodId();
            GrpcServiceSpecification service = services.get(serviceId);
            if (isNull(service)) {
                sendServiceNotExistsError(responseObserver, serviceId);
                return;
            }
            if (!service.getServiceTypes().contains(GRPC_SERVICE_TYPE)) {
                sendServiceNotExistsError(responseObserver, serviceId);
                return;
            }
            Map<String, GrpcMethod> grpcMethods = service.getGrpcService().getGrpcMethods();
            GrpcMethod grpcMethod = grpcMethods.get(serviceMethodId);
            if (isNull(grpcMethod)) {
                sendMethodNotExistsError(responseObserver, serviceMethodId);
                return;
            }
            for (ValueInterceptor<Entity, Entity> requestValueInterceptor : grpcMethod.requestValueInterceptors()) {
                ValueInterceptionResult<Entity, Entity> result = requestValueInterceptor.intercept(serviceRequestEntity);
                if (isNull(result)) {
                    break;
                }
                serviceRequestEntity = result.getOutValue();
                if (result.getNextInterceptionStrategy() == PROCESS_HANDLING) {
                    break;
                }
                if (result.getNextInterceptionStrategy() == STOP_HANDLING) {
                    if (isNull(result.getOutValue())) {
                        responseObserver.onNext(writeProtobuf(fromServiceResponse().map(okResponse(command))));
                        responseObserver.onCompleted();
                        return;
                    }
                    responseObserver.onNext(writeProtobuf(result.getOutValue()));
                    responseObserver.onCompleted();
                    return;
                }
            }
            EntityToModelMapper<ServiceRequest<?>> toServiceRequest = cast(toServiceRequest(cast(grpcMethod.requestMapper())));
            ServiceRequest<?> mappedServiceRequest = toServiceRequest.map(asEntity(serviceRequestEntity));
            ServiceRequest<?> serviceRequest = isEmpty(serviceRequestEntity.getValue(REQUEST_DATA)) || isNull(grpcMethod.requestMapper())
                    ? newServiceRequest(command, getOrElse(grpcMethod.validationPolicy(), NON_VALIDATABLE))
                    : newServiceRequest(command, mappedServiceRequest.getRequestData(), getOrElse(grpcMethod.validationPolicy(), NON_VALIDATABLE));
            ServiceResponse<?> serviceResponse = executeServiceMethodUnchecked(serviceRequest);
            handleResponse(responseObserver, grpcMethod, serviceResponse);
        }

        private void sendServiceNotExistsError(StreamObserver<com.google.protobuf.Value> responseObserver, String serviceId) {
            String errorMessage = format(GRPC_SERVICE_NOT_EXISTS_MESSAGE, serviceId);
            loggingModule()
                    .getLogger(GrpcServletContainer.class)
                    .error(errorMessage);
            responseObserver.onNext(writeProtobuf(fromServiceResponse().map(errorResponse(GRPC_SERVICE_NOT_EXISTS_CODE, errorMessage))));
            responseObserver.onCompleted();
        }

        private void sendMethodNotExistsError(StreamObserver<com.google.protobuf.Value> responseObserver, String methodId) {
            String errorMessage = format(GRPC_METHOD_NOT_EXISTS_MESSAGE, methodId);
            loggingModule()
                    .getLogger(GrpcServletContainer.class)
                    .error(errorMessage);
            responseObserver.onNext(writeProtobuf(fromServiceResponse().map(errorResponse(GRPC_METHOD_NOT_EXISTS_CODE, errorMessage))));
            responseObserver.onCompleted();
        }

        private void handleResponse(StreamObserver<com.google.protobuf.Value> responseObserver, GrpcMethod grpcMethod, ServiceResponse<?> serviceResponse) {
            EntityFromModelMapper<ServiceResponse<?>> fromServiceResponse = cast(fromServiceResponse(cast(grpcMethod.responseMapper())));
            Entity serviceResponseEntity = fromServiceResponse.map(serviceResponse);
            ServiceExecutionException serviceException = serviceResponse.getServiceException();
            for (ValueInterceptor<Entity, Entity> responseValueInterceptor : grpcMethod.responseValueInterceptors()) {
                ValueInterceptionResult<Entity, Entity> result = responseValueInterceptor.intercept(serviceResponseEntity);
                if (isNull(result)) {
                    break;
                }
                serviceResponseEntity = result.getOutValue();
                if (result.getNextInterceptionStrategy() == PROCESS_HANDLING) {
                    break;
                }
                if (result.getNextInterceptionStrategy() == STOP_HANDLING) {
                    if (isNull(result.getOutValue())) {
                        responseObserver.onNext(writeProtobuf(serviceResponseEntity));
                        responseObserver.onCompleted();
                        return;
                    }
                    responseObserver.onNext(writeProtobuf(serviceResponseEntity));
                    responseObserver.onCompleted();
                    return;
                }
            }
            if (nonNull(serviceException)) {
                loggingModule()
                        .getLogger(GrpcServletContainer.class)
                        .error(GRPC_SERVICE_EXCEPTION, serviceException);
            }
            responseObserver.onNext(writeProtobuf(serviceResponseEntity));
            responseObserver.onCompleted();
        }
    }
}