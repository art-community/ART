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

package ru.art.service;

import lombok.experimental.*;
import ru.art.service.interceptor.ServiceExecutionInterceptor.*;
import ru.art.service.model.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static java.util.Optional.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.constants.DateConstants.*;
import static ru.art.core.constants.InterceptionStrategy.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.logging.LoggingModuleConstants.LoggingParameters.*;
import static ru.art.logging.ThreadContextExtensions.*;
import static ru.art.service.ServiceModule.*;
import static ru.art.service.ServiceResponseDataExtractor.*;
import static ru.art.service.constants.RequestValidationPolicy.*;
import static ru.art.service.constants.ServiceErrorCodes.*;
import static ru.art.service.constants.ServiceExceptionsMessages.*;
import static ru.art.service.factory.ServiceRequestFactory.*;
import static ru.art.service.factory.ServiceResponseFactory.*;
import static ru.art.service.model.ServiceInterceptionResult.*;
import java.util.*;

@UtilityClass
public class ServiceController {
    public static <RequestType, ResponseType> Optional<ResponseType> executeServiceMethod(String serviceId, String methodId, RequestType requestData) {
        ServiceMethodCommand command = new ServiceMethodCommand(serviceId, methodId);
        ServiceRequest<RequestType> request = newServiceRequest(command, requestData);
        return ofNullable(extractResponseDataChecked(executeServiceMethodUnchecked(request)));
    }

    public static <ResponseType> Optional<ResponseType> executeServiceMethod(String serviceId, String methodId) {
        ServiceMethodCommand command = new ServiceMethodCommand(serviceId, methodId);
        ServiceRequest<?> request = newServiceRequest(command);
        return ofNullable(extractResponseDataChecked(executeServiceMethodUnchecked(request)));
    }

    public static <RequestType, ResponseType> ServiceResponse<ResponseType> executeServiceMethodUnchecked(String serviceId, String methodId, RequestType requestData) {
        ServiceMethodCommand command = new ServiceMethodCommand(serviceId, methodId);
        ServiceRequest<RequestType> request = newServiceRequest(command, requestData);
        return executeServiceMethodUnchecked(request);
    }

    public static <RequestType, ResponseType> ServiceResponse<ResponseType> executeServiceMethodValidatableUnchecked(String serviceId, String methodId, RequestType requestData) {
        ServiceMethodCommand command = new ServiceMethodCommand(serviceId, methodId);
        ServiceRequest<RequestType> request = newServiceRequest(command, requestData, VALIDATABLE);
        return executeServiceMethodUnchecked(request);
    }

    public static <ResponseType> ServiceResponse<ResponseType> executeServiceMethodUnchecked(String serviceId, String methodId) {
        ServiceMethodCommand command = new ServiceMethodCommand(serviceId, methodId);
        ServiceRequest<?> request = newServiceRequest(command);
        return executeServiceMethodUnchecked(request);
    }

    public static <RequestType, ResponseType> ServiceResponse<ResponseType> executeServiceMethodUnchecked(ServiceRequest<RequestType> request) {
        Date startTime = new Date();
        putIfNotNull(REQUEST_START_TIME_KEY, YYYY_MM_DD_HH_MM_SS_24H_Z_DOT_FORMAT.get().format(startTime));
        Specification service = serviceModuleState().getServiceRegistry().getService(request.getServiceMethodCommand().getServiceId());
        if (isNull(service)) {
            String errorMessage = format(SERVICE_WITH_ID_NOT_EXISTS, request.getServiceMethodCommand().getServiceId());
            return errorResponse(request.getServiceMethodCommand(), INTERNAL_ERROR, errorMessage);
        }
        ServiceInterceptionResult serviceInterceptionResult;
        if (nonNull((serviceInterceptionResult = beforeServiceExecution(service, request)).getResponse()))
            return cast(serviceInterceptionResult.getResponse());
        ServiceRequest<?> requestAfterInterception = serviceInterceptionResult.getRequest();
        ServiceResponse<ResponseType> response = service
                .getExceptionWrapper()
                .executeServiceWrapped(requestAfterInterception.getServiceMethodCommand(), requestAfterInterception);
        Date endTime = new Date();
        putIfNotNull(EXECUTION_TIME_KEY, endTime.getTime() - startTime.getTime());
        putIfNotNull(REQUEST_END_TIME_KEY, YYYY_MM_DD_HH_MM_SS_24H_Z_DOT_FORMAT.get().format(endTime));
        return cast(getOrElse(afterServiceExecution(request, service, response).getResponse(), response));
    }

    public static <RequestType> ServiceInterceptionResult beforeServiceExecution(Specification service, ServiceRequest<RequestType> request) {
        DeactivationConfig deactivationConfig = service.getDeactivationConfig();
        if (deactivationConfig.isDeactivated() || deactivationConfig.getDeactivatedMethods().contains(request.getServiceMethodCommand().getMethodId()))
            return stopHandling(request, okResponse(request.getServiceMethodCommand()));
        List<RequestInterceptor> methodRequestInterceptors = service.getMethodRequestInterceptors().get(request.getServiceMethodCommand().getMethodId());
        List<RequestInterceptor> requestInterceptors = service.getRequestInterceptors();
        ServiceInterceptionResult serviceInterceptionResult = nextInterceptor(request);
        for (RequestInterceptor interceptor : requestInterceptors) {
            serviceInterceptionResult = cast(interceptor.getInterception().intercept(serviceInterceptionResult.getRequest()));
            if (serviceInterceptionResult.getNextInterceptionStrategy() == PROCESS_HANDLING) break;
            if (serviceInterceptionResult.getNextInterceptionStrategy() == STOP_HANDLING) {
                return serviceInterceptionResult;
            }
        }

        if (isNull(methodRequestInterceptors)) {
            return serviceInterceptionResult;
        }

        for (RequestInterceptor interceptor : methodRequestInterceptors) {
            serviceInterceptionResult = cast(interceptor.getInterception().intercept(serviceInterceptionResult.getRequest()));
            if (serviceInterceptionResult.getNextInterceptionStrategy() == PROCESS_HANDLING) break;
            if (serviceInterceptionResult.getNextInterceptionStrategy() == STOP_HANDLING) {
                return serviceInterceptionResult;
            }
        }

        return serviceInterceptionResult;
    }

    public static <RequestType, ResponseType> ServiceInterceptionResult afterServiceExecution(ServiceRequest<RequestType> request, Specification service, ServiceResponse<ResponseType> response) {
        List<ResponseInterceptor> methodResponseInterceptors = service.getMethodResponseInterceptors().get(request.getServiceMethodCommand().getMethodId());
        List<ResponseInterceptor> responseInterceptors = service.getResponseInterceptors();
        ServiceInterceptionResult serviceInterceptionResult = nextInterceptor(request, response);
        for (ResponseInterceptor interceptor : responseInterceptors) {
            serviceInterceptionResult = cast(interceptor.getInterception().intercept(serviceInterceptionResult.getRequest(), response));
            if (serviceInterceptionResult.getNextInterceptionStrategy() == PROCESS_HANDLING) break;
            if (serviceInterceptionResult.getNextInterceptionStrategy() == STOP_HANDLING) {
                return serviceInterceptionResult;
            }
        }

        if (isNull(methodResponseInterceptors)) {
            return serviceInterceptionResult;
        }

        for (ResponseInterceptor interceptor : methodResponseInterceptors) {
            serviceInterceptionResult = cast(interceptor.getInterception().intercept(serviceInterceptionResult.getRequest(), response));
            if (serviceInterceptionResult.getNextInterceptionStrategy() == PROCESS_HANDLING) break;
            if (serviceInterceptionResult.getNextInterceptionStrategy() == STOP_HANDLING) {
                return serviceInterceptionResult;
            }
        }

        return serviceInterceptionResult;
    }
}
