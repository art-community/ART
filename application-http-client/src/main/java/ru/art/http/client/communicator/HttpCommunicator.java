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

import org.apache.http.*;
import org.apache.http.client.config.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.nio.client.*;
import ru.art.entity.*;
import ru.art.entity.interceptor.*;
import ru.art.entity.mapper.*;
import ru.art.http.client.constants.*;
import ru.art.http.client.handler.*;
import ru.art.http.client.interceptor.*;
import ru.art.http.client.model.*;
import ru.art.http.constants.*;
import static ru.art.http.client.constants.HttpClientModuleConstants.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

public interface HttpCommunicator {
    static HttpCommunicator httpCommunicator(String url) {
        return new HttpCommunicatorImplementation(url);
    }

    static HttpCommunicator httpCommunicator(HttpCommunicationTargetConfiguration targetConfiguration) {
        return new HttpCommunicatorImplementation(targetConfiguration);
    }

    HttpCommunicator client(CloseableHttpClient client);

    HttpCommunicator consumes(MimeToContentTypeMapper responseContentTypeMapper);

    HttpCommunicator ignoreResponseContentType();

    <ResponseType> HttpCommunicator responseMapper(ValueToModelMapper<ResponseType, ? extends Value> responseMapper);

    HttpCommunicator config(RequestConfig requestConfig);

    HttpCommunicator version(HttpVersion httpVersion);

    HttpCommunicator requestCharset(Charset charset);

    HttpCommunicator addPathParameter(String parameter);

    HttpCommunicator addRequestInterceptor(HttpClientInterceptor interceptor);

    HttpCommunicator addResponseInterceptor(HttpClientInterceptor interceptor);

    HttpCommunicator addQueryParameter(String name, String value);

    HttpCommunicator addHeader(String name, String value);

    HttpCommunicator get();

    HttpCommunicator post();

    HttpCommunicator put();

    HttpCommunicator patch();

    HttpCommunicator options();

    HttpCommunicator connectionClosingPolicy(ConnectionClosingPolicy policy);

    HttpCommunicator delete();

    HttpCommunicator trace();

    HttpCommunicator head();

    HttpCommunicator produces(MimeToContentTypeMapper requestContentTypeMapper);

    <RequestType> HttpCommunicator requestMapper(ValueFromModelMapper<RequestType, ? extends Value> requestMapper);

    HttpCommunicator chunked();

    HttpCommunicator gzipCompressed();

    HttpCommunicator requestEncoding(String encoding);

    HttpCommunicator enableKeepAlive();

    <RequestType, ResponseType> Optional<ResponseType> execute(RequestType request);

    <ResponseType> Optional<ResponseType> execute();

    HttpCommunicator addRequestValueInterceptor(ValueInterceptor<Value, Value> interceptor);

    HttpCommunicator addResponseValueInterceptor(ValueInterceptor<Value, Value> interceptor);

    void closeClient();

    HttpAsynchronousCommunicator asynchronous();


    interface HttpAsynchronousCommunicator {
        void closeAsynchronousClient();

        HttpAsynchronousCommunicator client(CloseableHttpAsyncClient client);

        <RequestType, ResponseType> HttpAsynchronousCommunicator completionHandler(HttpCommunicationResponseHandler<RequestType, ResponseType> handler);

        <RequestType> HttpAsynchronousCommunicator exceptionHandler(HttpCommunicationExceptionHandler<RequestType> handler);

        <RequestType> HttpAsynchronousCommunicator cancellationHandler(HttpCommunicationCancellationHandler<RequestType> handler);

        <RequestType, ResponseType> CompletableFuture<Optional<ResponseType>> executeAsynchronous(RequestType request);

        <ResponseType> CompletableFuture<Optional<ResponseType>> executeAsynchronous();
    }
}
