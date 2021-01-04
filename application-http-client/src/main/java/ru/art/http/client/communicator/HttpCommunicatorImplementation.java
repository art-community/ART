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

package ru.art.http.client.communicator;

import lombok.*;
import org.apache.http.*;
import org.apache.http.client.config.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.nio.client.*;
import org.apache.logging.log4j.*;
import ru.art.core.validator.*;
import ru.art.entity.Value;
import ru.art.entity.interceptor.*;
import ru.art.entity.mapper.*;
import ru.art.http.client.communicator.HttpCommunicator.*;
import ru.art.http.client.handler.*;
import ru.art.http.client.interceptor.*;
import ru.art.http.client.model.*;
import ru.art.http.constants.*;
import static java.util.Optional.*;
import static lombok.AccessLevel.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.core.extension.StringExtensions.*;
import static ru.art.core.wrapper.ExceptionWrapper.*;
import static ru.art.http.client.communicator.HttpCommunicationExecutor.*;
import static ru.art.http.client.constants.HttpClientModuleConstants.*;
import static ru.art.http.client.module.HttpClientModule.*;
import static ru.art.http.constants.HttpMethodType.*;
import static ru.art.logging.LoggingModule.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

public class HttpCommunicatorImplementation implements HttpCommunicator, HttpAsynchronousCommunicator {
    private final BuilderValidator validator = new BuilderValidator(HttpCommunicator.class.getName());
    private final HttpCommunicationConfiguration configuration = new HttpCommunicationConfiguration();
    @Getter(lazy = true, value = PRIVATE)
    private final static Logger logger = loggingModule().getLogger(HttpCommunicator.class);

    HttpCommunicatorImplementation(String url) {
        this(HttpCommunicationTargetConfiguration.httpCommunicationTarget().build().url(url));
    }

    HttpCommunicatorImplementation(HttpCommunicationTargetConfiguration targetConfiguration) {
        config(targetConfiguration.requestConfig());
        if (isNotEmpty(targetConfiguration.url())) {
            configuration.setUrl(targetConfiguration.url());
            return;
        }
        configuration.setUrl(validator.notEmptyField(targetConfiguration.scheme(), "scheme") + SCHEME_DELIMITER
                + validator.notEmptyField(targetConfiguration.host(), "host")
                + COLON + validator.notNullField(targetConfiguration.port(), "port")
                + validator.notEmptyField(targetConfiguration.path(), "path"));
    }

    @Override
    public HttpCommunicator get() {
        configuration.setMethodType(GET);
        return this;
    }

    @Override
    public HttpCommunicator post() {
        configuration.setMethodType(POST);
        return this;
    }

    @Override
    public HttpCommunicator put() {
        configuration.setMethodType(PUT);
        return this;
    }

    @Override
    public HttpCommunicator patch() {
        configuration.setMethodType(PATCH);
        return this;
    }

    @Override
    public HttpCommunicator options() {
        configuration.setMethodType(OPTIONS);
        return this;
    }

    @Override
    public HttpCommunicator delete() {
        configuration.setMethodType(DELETE);
        return this;
    }

    @Override
    public HttpCommunicator trace() {
        configuration.setMethodType(TRACE);
        return this;
    }

    @Override
    public HttpCommunicator head() {
        configuration.setMethodType(HEAD);
        return this;
    }

    @Override
    public HttpCommunicator produces(MimeToContentTypeMapper requestContentTypeMapper) {
        configuration.setProducesMimeType(validator.notNullField(requestContentTypeMapper, "requestContentTypeMapper"));
        return this;
    }

    @Override
    public <RequestType> HttpCommunicator requestMapper(ValueFromModelMapper<RequestType, ? extends Value> requestMapper) {
        configuration.setRequestMapper(validator.notNullField(cast(requestMapper), "requestMapper"));
        return this;
    }

    @Override
    public HttpCommunicator client(CloseableHttpClient client) {
        configuration.setSynchronousClient(validator.notNullField(getOrElse(client, httpClientModule().getClient()), "synchronousClient"));
        return this;
    }

    @Override
    public HttpCommunicator consumes(MimeToContentTypeMapper responseContentTypeMapper) {
        configuration.setConsumesMimeType(validator.notNullField(responseContentTypeMapper, "responseContentTypeMapper"));
        return this;
    }

    @Override
    public HttpCommunicator ignoreResponseContentType() {
        configuration.setIgnoreResponseContentType(true);
        return this;
    }

    @Override
    public <ResponseType> HttpCommunicator responseMapper(ValueToModelMapper<ResponseType, ? extends Value> responseMapper) {
        configuration.setResponseMapper(validator.notNullField(cast(responseMapper), "responseMapper"));
        return this;

    }

    @Override
    public HttpCommunicator config(RequestConfig requestConfig) {
        configuration.setRequestConfig(validator.notNullField(getOrElse(requestConfig, httpClientModule().getRequestConfig()), "requestConfig"));
        return this;
    }

    @Override
    public HttpCommunicator version(HttpVersion httpVersion) {
        HttpVersion version = getOrElse(httpVersion, httpClientModule().getHttpVersion());
        configuration.setHttpProtocolVersion(validator.notNullField(version, "httpVersion"));
        return this;
    }

    @Override
    public HttpCommunicator requestCharset(Charset requestContentCharset) {
        Charset charset = getOrElse(requestContentCharset, contextConfiguration().getCharset());
        configuration.setRequestContentCharset(validator.notNullField(charset, "requestContentCharset"));
        return this;
    }

    @Override
    public HttpCommunicator addPathParameter(String parameter) {
        configuration.getPathParameters().add(validator.notEmptyField(parameter, "pathParameter"));
        return this;
    }

    @Override
    public HttpCommunicator addRequestInterceptor(HttpClientInterceptor interceptor) {
        configuration.getRequestInterceptors().add(validator.notNullField(interceptor, "requestInterceptor"));
        return this;
    }

    @Override
    public HttpCommunicator addResponseInterceptor(HttpClientInterceptor interceptor) {
        configuration.getResponseInterceptors().add(validator.notNullField(interceptor, "responseInterceptor"));
        return this;
    }

    @Override
    public HttpCommunicator addQueryParameter(String name, String value) {
        configuration.getQueryParameters().put(validator.notEmptyField(name, "queryParameterName"), validator.notNullField(value, "queryParameterValue"));
        return this;
    }

    @Override
    public HttpCommunicator addHeader(String name, String value) {
        configuration.getHeaders().put(validator.notEmptyField(name, "headerName"), validator.notNullField(value, "headerValue"));
        return this;
    }

    @Override
    public HttpCommunicator chunked() {
        configuration.setChunkedBody(true);
        return this;
    }

    @Override
    public HttpCommunicator gzipCompressed() {
        configuration.setGzipCompressedBody(true);
        return this;
    }

    @Override
    public HttpCommunicator connectionClosingPolicy(ConnectionClosingPolicy policy) {
        configuration.setConnectionClosingPolicy(policy);
        return this;
    }

    @Override
    public HttpCommunicator requestEncoding(String encoding) {
        configuration.setRequestContentEncoding(emptyIfNull(encoding));
        return this;
    }

    @Override
    public HttpCommunicator enableKeepAlive() {
        configuration.setEnableKeepAlive(true);
        return this;
    }

    @Override
    public HttpCommunicator addRequestValueInterceptor(ValueInterceptor<Value, Value> interceptor) {
        configuration.getRequestValueInterceptors().add(validator.notNullField(interceptor, "requestValueInterceptor"));
        return this;
    }

    @Override
    public HttpCommunicator addResponseValueInterceptor(ValueInterceptor<Value, Value> interceptor) {
        configuration.getResponseValueInterceptors().add(validator.notNullField(interceptor, "responseValueInterceptor"));
        return this;
    }

    @Override
    public void closeClient() {
        ignoreException(configuration.getSynchronousClient()::close, getLogger()::error);
    }

    @Override
    public <RequestType, ResponseType> Optional<ResponseType> execute(RequestType request) {
        request = validator.notNullField(request, "request");
        validator.validate();
        return ofNullable(executeSynchronousHttpRequest(configuration, request));
    }

    @Override
    public <ResponseType> Optional<ResponseType> execute() {
        validator.validate();
        return ofNullable(executeSynchronousHttpRequest(configuration, null));
    }


    @Override
    public void closeAsynchronousClient() {
        ignoreException(configuration.getAsynchronousClient()::close, getLogger()::error);
    }

    @Override
    public HttpAsynchronousCommunicator client(CloseableHttpAsyncClient client) {
        configuration.setAsynchronousClient(validator.notNullField(client, "asynchronousClient"));
        return this;
    }

    @Override
    public <RequestType, ResponseType> HttpAsynchronousCommunicator completionHandler(HttpCommunicationResponseHandler<RequestType, ResponseType> handler) {
        configuration.setCompletionHandler(validator.notNullField(handler, "responseHandler"));
        return this;
    }

    @Override
    public <RequestType> HttpAsynchronousCommunicator exceptionHandler(HttpCommunicationExceptionHandler<RequestType> handler) {
        configuration.setExceptionHandler(validator.notNullField(handler, "exceptionHandler"));
        return this;
    }

    @Override
    public <RequestType> HttpAsynchronousCommunicator cancellationHandler(HttpCommunicationCancellationHandler<RequestType> handler) {
        configuration.setCancellationHandler(validator.notNullField(handler, "cancellationHandler"));
        return this;
    }

    @Override
    public <RequestType, ResponseType> CompletableFuture<Optional<ResponseType>> executeAsynchronous(RequestType request) {
        request = validator.notNullField(request, "request");
        validator.validate();
        return executeAsynchronousHttpRequest(configuration, request);
    }

    @Override
    public HttpAsynchronousCommunicator asynchronous() {
        return this;
    }

    @Override
    public <ResponseType> CompletableFuture<Optional<ResponseType>> executeAsynchronous() {
        validator.validate();
        return executeAsynchronousHttpRequest(configuration, null);
    }
}