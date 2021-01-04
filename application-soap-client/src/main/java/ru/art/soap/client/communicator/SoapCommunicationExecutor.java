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

package ru.art.soap.client.communicator;

import lombok.*;
import ru.art.entity.mapper.ValueFromModelMapper.*;
import ru.art.entity.mapper.ValueToModelMapper.*;
import ru.art.http.client.communicator.*;
import static java.util.Objects.*;
import static lombok.AccessLevel.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.http.client.communicator.HttpCommunicator.*;
import static ru.art.http.constants.HttpHeaders.*;
import static ru.art.soap.client.communicator.SoapEntityMapping.*;
import java.util.*;
import java.util.concurrent.*;

@NoArgsConstructor(access = PACKAGE)
class SoapCommunicationExecutor {
    static <RequestType, ResponseType> Optional<ResponseType> execute(SoapCommunicationConfiguration configuration, RequestType request) {
        final HttpCommunicator httpCommunicator = httpCommunicator(configuration.getUrl());
        configuration.getRequestInterceptors().forEach(httpCommunicator::addRequestInterceptor);
        configuration.getResponseInterceptors().forEach(httpCommunicator::addResponseInterceptor);
        configuration.getRequestValueInterceptors().forEach(interceptor -> httpCommunicator.addRequestValueInterceptor(cast(interceptor)));
        configuration.getResponseValueInterceptors().forEach(interceptor -> httpCommunicator.addResponseValueInterceptor(cast(interceptor)));
        if (configuration.isEnableKeepAlive()) {
            httpCommunicator.enableKeepAlive();
        }
        httpCommunicator
                .connectionClosingPolicy(configuration.getConnectionClosingPolicy())
                .version(configuration.getHttpVersion())
                .requestCharset(configuration.getRequestCharset())
                .addHeader(ACCEPT, configuration.getConsumesMimeType().getMimeType().toString())
                .addHeader(CONTENT_TYPE, configuration.getConsumesMimeType().getMimeType().toString());
        XmlEntityToModelMapper<?> responseMapper = soapResponseToModel(configuration);
        if (nonNull(responseMapper)) {
            httpCommunicator.responseMapper(responseMapper);
        }
        XmlEntityFromModelMapper<?> requestMapper = soapRequestFromModel(configuration);
        if (nonNull(requestMapper)) {
            httpCommunicator.requestMapper(requestMapper);
        }
        return httpCommunicator.config(configuration.getRequestConfig())
                .consumes(configuration.getConsumesMimeType().toHttpMimeToContentTypeMapper())
                .post()
                .requestEncoding(configuration.getRequestBodyEncoding())
                .produces(configuration.getProducesMimeType().toHttpMimeToContentTypeMapper())
                .client(configuration.getHttpClient())
                .execute(request);
    }

    static <RequestType, ResponseType> CompletableFuture<Optional<ResponseType>> executeAsynchronous(SoapCommunicationConfiguration configuration, RequestType request) {
        final HttpCommunicator httpCommunicator = httpCommunicator(configuration.getUrl());
        configuration.getRequestInterceptors().forEach(httpCommunicator::addRequestInterceptor);
        configuration.getRequestInterceptors().forEach(httpCommunicator::addResponseInterceptor);
        configuration.getRequestValueInterceptors().forEach(interceptor -> httpCommunicator.addRequestValueInterceptor(cast(interceptor)));
        configuration.getResponseValueInterceptors().forEach(interceptor -> httpCommunicator.addResponseValueInterceptor(cast(interceptor)));
        if (configuration.isEnableKeepAlive()) {
            httpCommunicator.enableKeepAlive();
        }
        httpCommunicator
                .connectionClosingPolicy(configuration.getConnectionClosingPolicy())
                .version(configuration.getHttpVersion())
                .requestCharset(configuration.getRequestCharset())
                .addHeader(ACCEPT, configuration.getConsumesMimeType().getMimeType().toString())
                .addHeader(CONTENT_TYPE, configuration.getConsumesMimeType().getMimeType().toString());
        XmlEntityToModelMapper<?> responseMapper = soapResponseToModel(configuration);
        if (nonNull(responseMapper)) {
            httpCommunicator.responseMapper(responseMapper);
        }
        XmlEntityFromModelMapper<?> requestMapper = soapRequestFromModel(configuration);
        if (nonNull(requestMapper)) {
            httpCommunicator.requestMapper(requestMapper);
        }
        return httpCommunicator.config(configuration.getRequestConfig())
                .consumes(configuration.getConsumesMimeType().toHttpMimeToContentTypeMapper())
                .post()
                .requestEncoding(configuration.getRequestBodyEncoding())
                .produces(configuration.getProducesMimeType().toHttpMimeToContentTypeMapper())
                .asynchronous()
                .completionHandler(configuration.getResponseHandler())
                .exceptionHandler(configuration.getExceptionHandler())
                .cancellationHandler(configuration.getCancellationHandler())
                .client(configuration.getAsynchronousHttpClient())
                .executeAsynchronous(request);
    }
}