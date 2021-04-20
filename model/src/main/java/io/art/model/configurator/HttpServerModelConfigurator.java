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

import io.art.core.collection.*;
import io.art.core.model.*;
import io.art.model.implementation.server.*;
import io.art.value.constants.ValueModuleConstants.*;
import io.netty.handler.ssl.*;
import lombok.*;
import reactor.netty.http.*;
import reactor.netty.http.server.*;
import reactor.netty.tcp.SslProvider;

import java.io.*;
import java.util.*;
import java.util.function.*;

import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.constants.NetworkConstants.*;
import static io.art.core.constants.StringConstants.SLASH;
import static io.art.core.factory.MapFactory.*;
import static io.art.core.factory.SetFactory.*;
import static io.art.http.constants.HttpModuleConstants.Defaults.*;
import static io.art.value.constants.ValueModuleConstants.DataFormat.*;
import static java.util.Objects.*;
import static java.util.function.UnaryOperator.*;
import static lombok.AccessLevel.*;

@Getter(value = PACKAGE)
@NoArgsConstructor
public class HttpServerModelConfigurator {
    private final Map<String, HttpServiceModelConfigurator> routes = map();
    private final Set<String> existentIDs = set();
    private String host = BROADCAST_IP_ADDRESS;
    private Integer port = DEFAULT_PORT;
    private HttpProtocol protocol = HttpProtocol.HTTP11;
    private boolean compression = false;
    private boolean logging = false;
    private boolean wiretap = false;
    private boolean accessLogging = false;
    private int fragmentationMtu = 0;
    private DataFormat defaultDataFormat = JSON;
    private final HttpServiceExceptionMappingConfigurator exceptionMapping = new HttpServiceExceptionMappingConfigurator();
    private UnaryOperator<HttpRequestDecoderSpec> requestDecoderConfigurator = identity();
    private SslContext defaultSslContext;
    private final Map<String, Consumer<? super SslProvider.SslContextSpec>> sniMapping = map();
    private boolean redirectToHttps = false;

    public HttpServerModelConfigurator route(String path, Class<?> serviceClass){
        return route(path, serviceClass, identity());
    }

    public HttpServerModelConfigurator route(String path, Class<?> serviceClass,
                                             UnaryOperator<HttpServiceModelConfigurator> configurator){
        addRouteIfAbsent(
                path.endsWith(SLASH) ? path : path + SLASH,
                configurator.apply(new HttpServiceModelConfigurator(serviceClass)
                        .logging(logging)
                        .defaultDataFormat(defaultDataFormat)));
        return this;
    }

    public HttpServerModelConfigurator exceptions(UnaryOperator<HttpServiceExceptionMappingConfigurator> configurator){
        configurator.apply(exceptionMapping);
        return this;
    }

    public HttpServerModelConfigurator host(String host) {
        this.host = host;
        return this;
    }

    public HttpServerModelConfigurator port(Integer port) {
        this.port = port;
        return this;
    }

    public HttpServerModelConfigurator protocol(HttpProtocol version) {
        this.protocol = version;
        return this;
    }

    public HttpServerModelConfigurator logging(boolean isEnabled) {
        logging = isEnabled;
        return this;
    }

    public HttpServerModelConfigurator wiretap(boolean isEnabled){
        wiretap = isEnabled;
        return this;
    }

    public HttpServerModelConfigurator accessLogging(boolean isEnabled){
        accessLogging = isEnabled;
        return this;
    }

    public HttpServerModelConfigurator compress() {
        compression = true;
        return this;
    }

    public HttpServerModelConfigurator fragmentationMtu(int mtu) {
        fragmentationMtu = mtu;
        return this;
    }

    public HttpServerModelConfigurator defaultDataFormat(DataFormat format) {
        defaultDataFormat = format;
        return this;
    }

    public HttpServerModelConfigurator configureRequestDecoder(UnaryOperator<HttpRequestDecoderSpec> configurator){
        requestDecoderConfigurator = configurator;
        return this;
    }

    @SneakyThrows
    public HttpServerModelConfigurator ssl(File certificate, File key){
        defaultSslContext = SslContextBuilder.forServer(certificate, key).build();
        return this;
    }

    @SneakyThrows
    public HttpServerModelConfigurator ssl(String domain, File certificate, File key){
        SslContext sslContext = SslContextBuilder.forServer(certificate, key).build();
        sniMapping.put(domain, spec -> spec.sslContext(sslContext));
        return this;
    }

    public HttpServerModelConfigurator redirectToHttps(boolean isEnabled){
        redirectToHttps = isEnabled;
        return this;
    }

    protected HttpServerModel configure() {
        ImmutableMap.Builder<String, HttpServiceModel> services = immutableMapBuilder();
        routes.forEach((path, modelConfigurator) -> services.put(path, modelConfigurator.configure(path)));
        return HttpServerModel.builder()
                .services(services.build())
                .host(host)
                .port(port)
                .protocol(protocol)
                .compression(compression)
                .logging(logging)
                .wiretap(wiretap)
                .accessLogging(accessLogging)
                .fragmentationMtu(fragmentationMtu)
                .defaultDataFormat(defaultDataFormat)
                .requestDecoderConfigurator(requestDecoderConfigurator)
                .redirectToHttps(redirectToHttps)
                .sslConfigurator(isNull(defaultSslContext) ? null :
                        spec -> spec.sslContext(defaultSslContext)
                                .addSniMappings(sniMapping)
                                .build())
                .exceptionsMapper(exceptionMapping.configure())
                .build();
    }

    private void addRouteIfAbsent(String route, HttpServiceModelConfigurator routeConfigurator){
        if (!existentIDs.contains(routeConfigurator.getId()) && !routes.containsKey(route)){
            existentIDs.add(routeConfigurator.getId());
            routes.put(route, routeConfigurator);
        }
    }

}
