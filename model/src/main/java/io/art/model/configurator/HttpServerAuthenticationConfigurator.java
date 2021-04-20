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

import io.art.http.authentication.Authenticator.*;
import io.art.http.authentication.*;
import java.util.function.*;
import lombok.*;
import reactor.netty.http.server.*;
import static io.art.http.authentication.HttpAuthenticator.*;
import static io.art.http.authentication.HttpAuthenticatorRegistry.*;

public class HttpServerAuthenticationConfigurator {
    @Getter(value=AccessLevel.PROTECTED)
    private final HttpAuthenticatorRegistry registry = httpAuthenticatorRegistry();

    public HttpServerAuthenticationConfigurator on(String path, Authenticator<HttpServerRequest, HttpServerResponse> authenticator){
        registry.add(path, authenticator);
        return this;
    }

    public HttpServerAuthenticationConfigurator basicHttp(String pathPattern, Function<String, AuthenticationStatus> credentialsChecker,
                                                          String realm, UnaryOperator<HttpServerResponse> onAllow){
        registry.add(pathPattern, basicHttpAuthentication(credentialsChecker, realm, onAllow));
        return this;
    }

    public HttpServerAuthenticationConfigurator basicHttp(String pathPattern, Function<String, AuthenticationStatus> credentialsChecker, String realm){
        registry.add(pathPattern, basicHttpAuthentication(credentialsChecker, realm));
        return this;
    }

    public HttpServerAuthenticationConfigurator orElseAllow(){
        registry.defaultAuthenticator(alwaysAllow());
        return this;
    }

    public HttpServerAuthenticationConfigurator orElseAllow(UnaryOperator<HttpServerResponse> onAllow){
        registry.defaultAuthenticator(alwaysAllow(onAllow));
        return this;
    }

    public HttpServerAuthenticationConfigurator orElseDeny(){
        registry.defaultAuthenticator(alwaysDeny());
        return this;
    }

    public HttpServerAuthenticationConfigurator orElseDeny(UnaryOperator<HttpServerResponse> onDeny){
        registry.defaultAuthenticator(alwaysDeny(onDeny));
        return this;
    }
}
