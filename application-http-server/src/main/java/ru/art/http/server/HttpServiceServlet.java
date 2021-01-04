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
import ru.art.http.constants.*;
import ru.art.http.mapper.*;
import ru.art.http.server.context.*;
import ru.art.http.server.context.HttpRequestContext.*;
import ru.art.http.server.context.MultiPartContext.*;
import ru.art.http.server.exception.*;
import ru.art.http.server.handler.*;
import ru.art.http.server.model.*;
import ru.art.logging.*;
import ru.art.service.model.*;
import static java.nio.charset.Charset.*;
import static java.text.MessageFormat.*;
import static java.util.Arrays.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static org.apache.logging.log4j.ThreadContext.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.isEmpty;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.core.extension.StringExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.core.mime.MimeType.valueOf;
import static ru.art.http.constants.HttpHeaders.*;
import static ru.art.http.constants.HttpMethodType.*;
import static ru.art.http.constants.HttpMimeTypes.*;
import static ru.art.http.constants.HttpStatus.*;
import static ru.art.http.server.HttpServerRequestHandler.*;
import static ru.art.http.server.body.descriptor.HttpBodyDescriptor.*;
import static ru.art.http.server.constants.HttpServerExceptionMessages.*;
import static ru.art.http.server.constants.HttpServerLoggingMessages.*;
import static ru.art.http.server.constants.HttpServerModuleConstants.*;
import static ru.art.http.server.module.HttpServerModule.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.logging.LoggingModuleConstants.*;
import static ru.art.logging.LoggingModuleConstants.LoggingParameters.*;
import static ru.art.logging.LoggingParametersManager.*;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

@AllArgsConstructor
@MultipartConfig
class HttpServiceServlet extends HttpServlet {
    private final Map<HttpMethodType, HttpServletCommand> commands;
    private final Map<MimeType, HttpContentMapper> contentMappers = concurrentHashMap(httpServerModule().getContentMappers());
    private final Map<Class<Throwable>, HttpExceptionHandler<Throwable>> exceptionHandlers = concurrentHashMap(httpServerModule().getExceptionHandlers());

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) {
        HttpMethodType httpMethodType = resolve(request.getMethod());
        HttpServletCommand command = commands.get(httpMethodType);
        if (OPTIONS == httpMethodType) {
            response.setHeader(ALLOW, commands.values()
                    .stream()
                    .map(method -> method.getHttpMethod().getMethodType().name())
                    .collect(joining(COMMA)));
            response.setStatus(OK.getCode());
            clearServiceCallLoggingParameters();
            return;
        }
        if (isNull(command)) {
            handleException(request, response, new HttpServerException(format(HTTP_METHOD_NOT_ALLOWED, request.getMethod())));
            clearServiceCallLoggingParameters();
            return;
        }
        ServiceMethodCommand serviceCommand = createServiceCommand(command.getServiceId(), command.getHttpMethod());
        clearServiceCallLoggingParameters();
        putServiceCallLoggingParameters(ServiceCallLoggingParameters.builder()
                .serviceId(command.getServiceId())
                .serviceMethodId(serviceCommand.toString())
                .serviceMethodCommand(serviceCommand.toString() + DOT + getOrElse(get(REQUEST_ID_KEY), DEFAULT_REQUEST_ID))
                .logEventType(HTTP_SERVLET_EVENT)
                .build());
        try {
            updateRequestContext(command, request, response);
            byte[] responseBody = executeHttpService(serviceCommand, request, command.getHttpMethod());
            if (!command.getHttpMethod().isOverrideResponseContentType()) {
                response.addHeader(CONTENT_TYPE, httpServerModuleState().getRequestContext().getAcceptType().toString());
            }
            writeResponseBody(response, responseBody);
            clearServiceCallLoggingParameters();
            httpServerModuleState().clearRequestContext();
        } catch (Throwable throwable) {
            handleException(request, response, throwable);
            clearServiceCallLoggingParameters();
        }
    }

    private void handleException(HttpServletRequest request, HttpServletResponse response, Throwable exception) {
        loggingModule().getLogger(HttpServiceServlet.class).error(HTTP_REQUEST_HANDLING_EXCEPTION_MESSAGE, exception);
        Class<? extends Throwable> exceptionClass = exception.getClass();
        HttpExceptionHandler<Throwable> exceptionExceptionHandler = cast(exceptionHandlers.get(exceptionClass));
        if (isNull(exceptionExceptionHandler)) {
            exceptionExceptionHandler = cast(exceptionHandlers.get(Throwable.class));
        }
        if (isNull(exceptionExceptionHandler)) throw new HttpServerException(exception);
        exceptionExceptionHandler.handle(exception, request, response);
    }

    private void updateRequestContext(HttpServletCommand command, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> headers = mapOf();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        List<Cookie> cookies = fixedArrayOf(request.getCookies());
        String charsetHeader = request.getHeader(ACCEPT_CHARSET);
        HttpRequestContextBuilder requestContextBuilder = HttpRequestContext.builder()
                .requestUrl(request.getRequestURL().toString())
                .request(request)
                .response(response)
                .headers(headers)
                .cookies(cookies)
                .acceptCharset(isEmpty(charsetHeader) ? contextConfiguration().getCharset() : forName(charsetHeader))
                .acceptEncodings(emptyIfNull(request.getHeader(ACCEPT_ENCODING)))
                .acceptLanguages(emptyIfNull(request.getHeader(ACCEPT_LANGUAGE)))
                .contentLength(request.getContentLength())
                .hasContent(request.getContentLength() == EMPTY_HTTP_CONTENT_LENGTH);
        calculateAcceptType(command, request, response, requestContextBuilder);
        calculateContentType(command, request, requestContextBuilder);
        String contentType;
        if (nonNull(contentType = request.getHeader(CONTENT_TYPE)) && contentType.contains(MULTIPART_PATTERN)) {
            calculateParts(request, requestContextBuilder);
        }
        httpServerModuleState().setRequestContext(requestContextBuilder.build());
    }

    private void calculateParts(HttpServletRequest request, HttpRequestContextBuilder requestContextBuilder) {
        Collection<Part> parts;
        try {
            parts = request.getParts();
        } catch (IOException ioException) {
            throw new HttpServerException(CANT_READ_MULTIPART_DATA_TERMINATE, ioException);
        } catch (ServletException servletException) {
            loggingModule()
                    .getLogger(HttpServiceServlet.class)
                    .error(CANT_READ_MULTIPART_DATA_SKIP, servletException);
            return;
        }
        parts.forEach(part -> addPartToContext(requestContextBuilder, part));
    }

    private void calculateAcceptType(HttpServletCommand command, HttpServletRequest request, HttpServletResponse response, HttpRequestContextBuilder requestContextBuilder) {
        if (command.isIgnoreRequestAcceptType()) {
            requestContextBuilder.acceptType(getProducesMimeTypeChecked(command));
            return;
        }
        if (command.getHttpMethod().isOverrideResponseContentType()) {
            MimeType responseContentType = valueOf(response.getContentType());
            if (contentMappers.containsKey(responseContentType)) {
                requestContextBuilder.acceptType(responseContentType);
                requestContextBuilder.interceptedResponseContentType(responseContentType);
                return;
            }
        }
        String acceptTypeHeader = request.getHeader(ACCEPT);
        if (isEmpty(acceptTypeHeader)) {
            requestContextBuilder.acceptType(getProducesMimeTypeChecked(command));
            return;
        }
        String[] acceptTypesStr = emptyIfNull(acceptTypeHeader).split(COMMA);
        if (isEmpty(acceptTypesStr)) {
            requestContextBuilder.acceptType(getProducesMimeTypeChecked(command));
            return;
        }
        List<MimeType> acceptTypes = sortMimeTypes(acceptTypesStr);
        for (MimeType type : acceptTypes) {
            if (contentMappers.containsKey(type)) {
                requestContextBuilder.acceptType(type);
                return;
            }
        }
        throw new HttpServerException(format(REQUEST_ACCEPT_TYPE_NOT_SUPPORTED, acceptTypeHeader));
    }

    private void calculateContentType(HttpServletCommand command, HttpServletRequest request, HttpRequestContextBuilder requestContextBuilder) {
        if (request.getContentLength() == EMPTY_HTTP_CONTENT_LENGTH) return;
        if (command.isIgnoreRequestContentType()) {
            requestContextBuilder.contentType(getConsumesMimeTypeChecked(command));
            return;
        }
        String contentTypeHeader = request.getContentType();
        if (isEmpty(contentTypeHeader)) {
            requestContextBuilder.contentType(getConsumesMimeTypeChecked(command));
            return;
        }
        String[] contentTypesStr = emptyIfNull(contentTypeHeader).split(COMMA);
        if (isEmpty(contentTypesStr)) {
            requestContextBuilder.contentType(getConsumesMimeTypeChecked(command));
            return;
        }
        List<MimeType> contentTypes = sortMimeTypes(contentTypesStr);
        for (MimeType type : contentTypes) {
            if (contentMappers.containsKey(type)) {
                requestContextBuilder.contentType(type);
                return;
            }
        }
        throw new HttpServerException(format(REQUEST_CONTENT_TYPE_NOT_SUPPORTED, contentTypeHeader));
    }

    private MimeType getConsumesMimeTypeChecked(HttpServletCommand command) {
        MimeToContentTypeMapper type;
        if (isNull(type = command.getConsumesMimeType())) {
            return ALL;
        }
        return type.getMimeType();
    }

    private MimeType getProducesMimeTypeChecked(HttpServletCommand command) {
        MimeToContentTypeMapper type;
        if (isNull(type = command.getProducesMimeType())) {
            return ALL;
        }
        return type.getMimeType();
    }

    private List<MimeType> sortMimeTypes(String[] mimeTypes) {
        return stream(mimeTypes).map(MimeType::valueOf).sorted().collect(toList());
    }

    private ServiceMethodCommand createServiceCommand(String serviceId, HttpService.HttpMethod methodConfig) {
        return new ServiceMethodCommand(serviceId, methodConfig.getMethodId());
    }

    private void addPartToContext(HttpRequestContextBuilder requestContextBuilder, Part part) {
        MultiPartContextBuilder partBuilder = MultiPartContext.builder()
                .contentLength(part.getSize())
                .contentType(valueOf(part.getContentType()))
                .fileName(part.getSubmittedFileName());
        part.getHeaderNames().forEach(headerName -> partBuilder.header(headerName, part.getHeader(headerName)));
        requestContextBuilder.part(part.getName(), partBuilder.build());
    }
}
