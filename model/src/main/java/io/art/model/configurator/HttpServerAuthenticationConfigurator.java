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

package io.art.model.configurator;

import io.art.http.authentication.*;
import io.art.http.authentication.Authenticator.*;
import java.util.function.*;
import lombok.*;
import reactor.netty.http.server.*;
import static io.art.http.authentication.HttpAuthenticatorFactory.*;
import static io.art.http.authentication.HttpAuthenticationRouter.*;

public class HttpServerAuthenticationConfigurator {
    @Getter(value=AccessLevel.PROTECTED)
    private final HttpAuthenticationRouter router = httpAuthenticationRouter();

    public HttpServerAuthenticationConfigurator custom(Authenticator<HttpServerRequest, HttpServerResponse> authenticator,
                                                       UnaryOperator<AuthenticationMethod> configurator) {
        router.add(authenticator, configurator);
        return this;
    }

    public HttpServerAuthenticationConfigurator custom(Function<HttpServerRequest, AuthenticationStatus> authenticationChecker,
                                                      UnaryOperator<AuthenticationMethod> configurator){
        router.add(customAuthentication(authenticationChecker), configurator);
        return this;
    }

    public HttpServerAuthenticationConfigurator header(Predicate<String> credentialsChecker, UnaryOperator<String> headerDecoder,
                                                      UnaryOperator<AuthenticationMethod> configurator){
        router.add(customHeaderAuthentication(credentialsChecker, headerDecoder), configurator);
        return this;
    }

    public HttpServerAuthenticationConfigurator basic(Predicate<String> credentialsChecker,
                                                      String realm,
                                                      UnaryOperator<AuthenticationMethod> configurator){
        router.add(basicAuthentication(credentialsChecker, realm), configurator);
        return this;
    }

    public HttpServerAuthenticationConfigurator basic(Predicate<String> credentialsChecker,
                                                      UnaryOperator<AuthenticationMethod> configurator){
        router.add(basicAuthentication(credentialsChecker), configurator);
        return this;
    }


    public HttpServerAuthenticationConfigurator bearer(Predicate<String> credentialsChecker,
                                                       UnaryOperator<AuthenticationMethod> configurator) {
        router.add(bearerAuthentication(credentialsChecker), configurator);
        return this;
    }


    public HttpServerAuthenticationConfigurator allow(UnaryOperator<AuthenticationMethod> configurator){
        return custom(alwaysAllow(), configurator);
    }

    public HttpServerAuthenticationConfigurator allow(String... paths){
        return custom(alwaysAllow(), allow -> allow.on(paths));
    }

    public HttpServerAuthenticationConfigurator allow(UnaryOperator<HttpServerResponse> onAllow,
                                                      UnaryOperator<AuthenticationMethod> configurator){
        return custom(alwaysAllow(onAllow), configurator);
    }

    public HttpServerAuthenticationConfigurator deny(UnaryOperator<AuthenticationMethod> configurator){
        return custom(alwaysDeny(), configurator);
    }

    public HttpServerAuthenticationConfigurator deny(String... paths){
        return custom(alwaysDeny(), a -> a.on(paths));
    }

    public HttpServerAuthenticationConfigurator deny(UnaryOperator<HttpServerResponse> onDeny,
                                                     UnaryOperator<AuthenticationMethod> configurator){
        return custom(alwaysDeny(onDeny), configurator);
    }





    public HttpServerAuthenticationConfigurator orElseAllow(UnaryOperator<HttpServerResponse> onAllow){
        return defaultAuthenticator(alwaysAllow(onAllow));
    }

    public HttpServerAuthenticationConfigurator orElseDeny(){
        return defaultAuthenticator(alwaysDeny());
    }

    public HttpServerAuthenticationConfigurator orElseDeny(UnaryOperator<HttpServerResponse> onDeny){
        return defaultAuthenticator(alwaysDeny(onDeny));
    }

    protected HttpServerAuthenticationConfigurator defaultAuthenticator(Authenticator<HttpServerRequest, HttpServerResponse> authenticator){
        router.defaultAuthenticator(authenticator);
        return this;
    }
}
