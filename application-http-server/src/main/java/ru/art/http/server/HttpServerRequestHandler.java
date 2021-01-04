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

package ru.art.http.server;

import lombok.*;
import ru.art.core.mime.*;
import ru.art.entity.Value;
import ru.art.entity.*;
import ru.art.entity.interceptor.*;
import ru.art.entity.mapper.*;
import ru.art.http.constants.*;
import ru.art.http.mapper.*;
import ru.art.http.server.exception.*;
import ru.art.http.server.model.HttpService.*;
import ru.art.service.exception.*;
import ru.art.service.model.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static lombok.AccessLevel.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.isEmpty;
import static ru.art.core.constants.ArrayConstants.*;
import static ru.art.core.constants.InterceptionStrategy.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.InputStreamExtensions.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.entity.Entity.*;
import static ru.art.http.server.body.descriptor.HttpBodyDescriptor.*;
import static ru.art.http.server.constants.HttpServerExceptionMessages.*;
import static ru.art.http.server.module.HttpServerModule.*;
import static ru.art.http.server.parser.HttpParametersParser.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.service.ServiceController.*;
import static ru.art.service.factory.ServiceRequestFactory.*;
import static ru.art.service.mapping.ServiceResponseMapping.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;

@NoArgsConstructor(access = PRIVATE)
class HttpServerRequestHandler {
    static byte[] executeHttpService(ServiceMethodCommand command, HttpServletRequest request, HttpMethod httpMethod) {
        Value requestValue = parseRequestValue(request, httpMethod);
        MimeType responseContentType = httpServerModuleState().getRequestContext().getAcceptType();
        HttpContentMapper contentMapper = httpServerModule().getContentMappers().get(responseContentType);
        Charset acceptCharset = httpServerModuleState().getRequestContext().getAcceptCharset();
        List<ValueInterceptor<Value, Value>> requestValueInterceptors = httpMethod.getRequestValueInterceptors();
        for (ValueInterceptor<Value, Value> requestValueInterceptor : requestValueInterceptors) {
            ValueInterceptionResult<Value, Value> result = requestValueInterceptor.intercept(requestValue);
            if (isNull(result)) {
                break;
            }
            requestValue = result.getOutValue();
            if (result.getNextInterceptionStrategy() == PROCESS_HANDLING) {
                break;
            }
            if (result.getNextInterceptionStrategy() == STOP_HANDLING) {
                if (isNull(result.getOutValue())) return EMPTY_BYTES;
                return contentMapper.getToContent().mapToBytes(result.getOutValue(), responseContentType, acceptCharset);
            }
        }
        ValueToModelMapper<?, Value> requestMapper;
        ServiceRequest<?> serviceRequest = isNull(httpMethod.getRequestDataSource()) || isNull(requestMapper = cast(httpMethod.getRequestMapper()))
                ? newServiceRequest(command, httpMethod.getRequestValidationPolicy())
                : newServiceRequest(command, requestMapper.map(requestValue), httpMethod.getRequestValidationPolicy());
        ServiceResponse<?> serviceResponse = executeServiceMethodUnchecked(serviceRequest);
        Value responseValue = mapResponseValue(httpMethod, serviceResponse);
        List<ValueInterceptor<Value, Value>> responseValueInterceptors = isNull(serviceResponse.getServiceException())
                ? httpMethod.getResponseValueInterceptors()
                : httpMethod.getExceptionValueInterceptors();
        for (ValueInterceptor<Value, Value> responseValueInterceptor : responseValueInterceptors) {
            ValueInterceptionResult<Value, Value> result = responseValueInterceptor.intercept(responseValue);
            if (isNull(result)) {
                break;
            }
            responseValue = result.getOutValue();
            if (result.getNextInterceptionStrategy() == PROCESS_HANDLING) {
                break;
            }
            if (result.getNextInterceptionStrategy() == STOP_HANDLING) {
                if (isNull(result.getOutValue())) return EMPTY_BYTES;
                return contentMapper.getToContent().mapToBytes(result.getOutValue(), responseContentType, acceptCharset);
            }
        }
        if (isNull(responseValue)) return EMPTY_BYTES;
        return contentMapper.getToContent().mapToBytes(responseValue, responseContentType, acceptCharset);
    }

    private static Value parseRequestValue(HttpServletRequest request, HttpMethod methodConfig) {
        MimeType requestContentType = httpServerModuleState().getRequestContext().getContentType();
        HttpRequestDataSource requestDataSource;
        if (isNull(requestDataSource = methodConfig.getRequestDataSource())) {
            return null;
        }
        switch (requestDataSource) {
            case BODY:
                if (httpServerModuleState().getRequestContext().isHasContent()) return null;
                HttpContentMapper contentMapper = httpServerModule().getContentMappers().get(requestContentType);
                Value value = contentMapper
                        .getFromContent()
                        .mapFromBytes(readRequestBody(request), requestContentType,
                                getOrElse(requestContentType.getCharset(), contextConfiguration().getCharset()));
                if (isNull(value)) return null;
                return value;
            case PATH_PARAMETERS:
                return parsePathParameters(request, methodConfig);
            case QUERY_PARAMETERS:
                return parseQueryParameters(request);
            case MULTIPART:
                Entity multipartEntity = readMultiParts(request);
                if (isNull(multipartEntity) || multipartEntity.isEmpty()) return null;
                return multipartEntity;
            default:
                throw new HttpServerException(format(UNKNOWN_HTTP_REQUEST_DATA_SOURCE, methodConfig.getRequestDataSource()));
        }
    }

    private static Entity readMultiParts(HttpServletRequest request) {
        if (httpServerModuleState().getRequestContext().isHasContent()) return null;
        Collection<Part> parts;
        try {
            parts = request.getParts();
        } catch (Exception throwable) {
            throw new HttpServerException(throwable);
        }
        EntityBuilder entityBuilder = entityBuilder();
        for (Part part : parts) {
            try {
                String submittedFileName = part.getSubmittedFileName();
                if (isEmpty(submittedFileName)) {
                    continue;
                }
                InputStream inputStream = part.getInputStream();
                byte[] value = toByteArray(inputStream);
                if (!isEmpty(value)) {
                    entityBuilder.byteArrayField(submittedFileName, value);
                }
            } catch (IOException ioException) {
                loggingModule()
                        .getLogger(HttpServerRequestHandler.class)
                        .error(EXCEPTION_OCCURRED_DURING_READING_PART, ioException);
            }
        }
        return entityBuilder.build();
    }

    private static Value mapResponseValue(HttpMethod httpMethod, ServiceResponse<?> serviceResponse) {
        ServiceExecutionException serviceException = serviceResponse.getServiceException();
        switch (httpMethod.getResponseHandlingMode()) {
            case UNCHECKED:
                return mapResponseObject(serviceResponse, fromServiceResponse(cast(httpMethod.getResponseMapper())));
            case CHECKED:
                if (isNull(serviceException)) {
                    Object responseData = serviceResponse.getResponseData();
                    if (nonNull(responseData)) {
                        ValueFromModelMapper responseMapper;
                        if (nonNull(responseMapper = httpMethod.getResponseMapper())) {
                            return mapResponseObject(responseData, cast(responseMapper));
                        }
                    }
                    return null;
                }
                ValueFromModelMapper exceptionMapper;
                if (isNull(exceptionMapper = httpMethod.getExceptionMapper())) {
                    throw serviceException;
                }
                return mapResponseObject(serviceException, cast(exceptionMapper));
            default:
                throw new HttpServerException(HTTP_RESPONSE_MODE_IS_NULL);
        }
    }

    private static Value mapResponseObject(Object object, ValueFromModelMapper<?, ? extends Value> mapper) {
        MimeType responseContentType = httpServerModuleState().getRequestContext().getAcceptType();
        HttpContentMapper contentMapper = httpServerModule().getContentMappers().get(responseContentType);
        return mapper.map(cast(object));
    }

}
