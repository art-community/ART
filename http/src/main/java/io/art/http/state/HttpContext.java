/*
 * ART
 *
 * Copyright 2019-2021 ART
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

package io.art.http.state;

import io.art.core.collection.*;
import io.art.http.constants.HttpModuleConstants.*;
import io.art.value.immutable.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import java.net.*;
import java.util.*;
import lombok.*;
import reactor.netty.http.server.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.factory.MapFactory.*;
import static io.art.value.factory.PrimitivesFactory.*;

@Getter
public class HttpContext {
    private final HttpServerRequest request;
    @Getter(value = AccessLevel.PRIVATE)
    private final HttpServerResponse response;
    private final ImmutableMap<String, Primitive> pathParams;
    private final ImmutableMap<String, Primitive> queryParams;
    private final ImmutableMap<CharSequence, Set<Cookie>> cookies;
    private final HttpHeaders headers;
    private final String scheme;
    private final InetSocketAddress hostAddress;
    private final InetSocketAddress remoteAddress;

    private HttpContext(HttpServerRequest request, HttpServerResponse response) {
        this.request = request;
        this.response = response;
        pathParams = let(
                request.params(),
                params -> params.entrySet()
                        .stream()
                        .collect(immutableMapCollector(Map.Entry::getKey, entry -> stringPrimitive(entry.getValue()))),
                emptyImmutableMap()
        );
        queryParams = parseQuery(request);
        cookies = immutableMapOf(request.cookies());
        headers = request.requestHeaders();
        scheme = request.scheme();
        hostAddress = request.hostAddress();
        remoteAddress = request.remoteAddress();
    }

    public static HttpContext from(HttpServerRequest request, HttpServerResponse response){
        return new HttpContext(request, response);
    }

    public HttpResponseStatus responseStatus(){
        return response.status();
    }

    public HttpHeaders responseHeaders(){
        return response.responseHeaders();
    }

    public Map<CharSequence, Set<Cookie>> responseCookies() {
        return response.cookies();
    }

    public HttpContext status(int status){
        response.status(status);
        return this;
    }

    public HttpContext status(HttpResponseStatus status){
        response.status(status);
        return this;
    }

    public HttpContext cookie(Cookie cookie){
        response.addCookie(cookie);
        return this;
    }

    public HttpContext header(CharSequence name, CharSequence value){
        response.header(name, value);
        return this;
    }

    public HttpContext headers(HttpHeaders headers){
        response.headers(headers);
        return this;
    }

    public HttpContext compression(boolean compress){
        response.compression(compress);
        return this;
    }

    public HttpContext redirect(String location){
        response
                .header("Location", location)
                .status(302);
        return this;
    }

    public HttpContext redirect(String location, HttpRedirectCode redirectCode){
        response
                .header("Location", location)
                .status(redirectCode.get());
        return this;
    }

    private static ImmutableMap<String, Primitive> parseQuery(HttpServerRequest request){
        String[] parts = request.uri().split(request.path());
        return parts.length != 2 ? emptyImmutableMap() :
                Arrays.stream(parts[1].substring(1).split("&"))
                        .sequential()
                        .map(query -> query.split("="))
                        .collect(immutableMapCollector(item -> item[0], item -> stringPrimitive(item[1])));
    }

}
