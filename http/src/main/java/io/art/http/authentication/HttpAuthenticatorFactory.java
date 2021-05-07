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

package io.art.http.authentication;

import io.art.http.authentication.Authenticator.*;
import io.netty.handler.codec.http.*;
import java.util.*;
import java.util.function.*;
import lombok.experimental.*;
import reactor.netty.http.server.*;
import static io.art.core.constants.EmptyFunctions.*;
import static io.art.http.authentication.Authenticator.*;
import static java.util.Objects.*;

@UtilityClass
public class HttpAuthenticatorFactory {

    public static Authenticator<HttpServerRequest, HttpServerResponse> basicHttpAuthentication(
            Predicate<String> credentialsChecker,
            String realm,
            UnaryOperator<HttpServerResponse> onAllow){

        AuthenticatorBuilder<HttpServerRequest, HttpServerResponse> builder = authenticatorBuilder();
        return builder
                .authenticationChecker((HttpServerRequest request) -> checkHttpAuthentication(request, credentialsChecker))
                .onUnauthorized(response -> response
                        .status(HttpResponseStatus.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Basic realm=\"" + realm +"\""))
                .onDeny(response -> response
                        .status(HttpResponseStatus.FORBIDDEN))
                .onAllow(onAllow)
                .build();
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> basicHttpAuthentication(
            Predicate<String> credentialsChecker, String realm){
        return basicHttpAuthentication(credentialsChecker, realm, emptyUnaryOperator());
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> alwaysAllow(UnaryOperator<HttpServerResponse> onAllow){
        AuthenticatorBuilder<HttpServerRequest, HttpServerResponse> builder = authenticatorBuilder();
        return builder
                .onAllow(onAllow)
                .build();
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> alwaysAllow(){
        return alwaysAllow(emptyUnaryOperator());
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> alwaysDeny(UnaryOperator<HttpServerResponse> onDeny){
        AuthenticatorBuilder<HttpServerRequest, HttpServerResponse> builder = authenticatorBuilder();
        return builder
                .authenticationChecker(request -> AuthenticationStatus.deny)
                .onDeny(onDeny)
                .build();
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> alwaysDeny(){
        return alwaysDeny(response -> response.status(HttpResponseStatus.FORBIDDEN));
    }

    private static AuthenticationStatus checkHttpAuthentication(HttpServerRequest request, Predicate<String> credentialsChecker){
        String header = request.requestHeaders().get("Authorization");
        if (isNull(header)) return AuthenticationStatus.unauthenticated;
        return credentialsChecker.test(decodeBasicAuthHeader(header)) ? AuthenticationStatus.allow : AuthenticationStatus.deny;
    }

    private static String decodeBasicAuthHeader(String header){
        if (!header.startsWith("Basic ")) throw new IllegalArgumentException("Not an Http Basic auth");
        return new String(Base64.getDecoder().decode(header.replace("Basic ", "")));
    }
}
