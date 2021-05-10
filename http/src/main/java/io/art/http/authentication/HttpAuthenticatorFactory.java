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
import static io.art.http.constants.HttpModuleConstants.HttpAuthentication.*;
import static java.util.Objects.*;

@UtilityClass
public class HttpAuthenticatorFactory {

    public static Authenticator<HttpServerRequest, HttpServerResponse> customAuthentication(Function<HttpServerRequest, AuthenticationStatus> authenticationChecker) {
        return authentication(authenticationChecker).build();
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> customHeaderAuthentication(Predicate<String> credentialsChecker, UnaryOperator<String> headerDecoder) {
        return headerAuthentication(credentialsChecker, headerDecoder).build();
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> basicAuthentication(Predicate<String> credentialsChecker) {
        return headerAuthentication(credentialsChecker, HttpAuthenticatorFactory::decodeBasicHeader)
                .unauthenticated(response -> response
                        .status(HttpResponseStatus.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Basic realm=\"\""))
                .build();
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> basicAuthentication(Predicate<String> credentialsChecker, String realm) {
        return headerAuthentication(credentialsChecker, HttpAuthenticatorFactory::decodeBasicHeader)
                .unauthenticated(response -> response
                        .status(HttpResponseStatus.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Basic realm=\"" + realm +"\""))
                .build();
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> bearerAuthentication(Predicate<String> credentialsChecker) {
        return headerAuthentication(credentialsChecker, HttpAuthenticatorFactory::decodeBearerHeader).build();
    }



    public static Authenticator<HttpServerRequest, HttpServerResponse> alwaysAllow(UnaryOperator<HttpServerResponse> onAllow) {
        AuthenticatorBuilder<HttpServerRequest, HttpServerResponse> builder = authenticatorBuilder();
        return builder
                .passed(onAllow)
                .build();
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> alwaysAllow() {
        return alwaysAllow(emptyUnaryOperator());
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> alwaysDeny(UnaryOperator<HttpServerResponse> onDeny) {
        AuthenticatorBuilder<HttpServerRequest, HttpServerResponse> builder = authenticatorBuilder();
        return builder
                .authenticationChecker(request -> AuthenticationStatus.deny)
                .failed(onDeny)
                .build();
    }

    public static Authenticator<HttpServerRequest, HttpServerResponse> alwaysDeny() {
        return alwaysDeny(response -> response.status(HttpResponseStatus.FORBIDDEN));
    }



    private static AuthenticatorBuilder<HttpServerRequest, HttpServerResponse> authentication(
            Function<HttpServerRequest, AuthenticationStatus> authenticator)
    {
        AuthenticatorBuilder<HttpServerRequest, HttpServerResponse> builder = authenticatorBuilder();
        return builder
                .authenticationChecker(authenticator)
                .unauthenticated(response -> response
                        .status(HttpResponseStatus.UNAUTHORIZED))
                .failed(response -> response
                        .status(HttpResponseStatus.FORBIDDEN));
    }

    private static AuthenticatorBuilder<HttpServerRequest, HttpServerResponse> headerAuthentication(
            Predicate<String> credentialsChecker,
            UnaryOperator<String> headerDecoder)
    {
        return authentication((HttpServerRequest request) -> checkAuthentication(request, credentialsChecker, headerDecoder));
    }

    private static AuthenticationStatus checkAuthentication(HttpServerRequest request, Predicate<String> credentialsChecker, UnaryOperator<String> headerDecoder) {
        String authenticationData = headerDecoder.apply(request.requestHeaders().get(AUTHORIZATION_HEADER));
        if (isNull(authenticationData)) return AuthenticationStatus.unauthenticated;
        return credentialsChecker.test(authenticationData) ? AuthenticationStatus.allow : AuthenticationStatus.deny;
    }


    private static String decodeBearerHeader(String header) {
        if (isNull(header) || !header.startsWith(BEARER_PREFIX)) return null;
        return header.substring(BEARER_PREFIX_LENGTH);
    }

    private static String decodeBasicHeader(String header) {
        if (isNull(header) || !header.startsWith(BASIC_PREFIX)) return null;
        return new String(Base64.getDecoder().decode(header.substring(BASIC_PREFIX_LENGTH)));
    }
}
