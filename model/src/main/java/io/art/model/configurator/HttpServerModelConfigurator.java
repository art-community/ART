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
import io.art.model.modeling.server.*;
import io.art.value.constants.ValueModuleConstants.*;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.*;
import java.io.*;
import java.util.*;
import java.util.function.*;
import lombok.*;
import reactor.netty.http.*;
import reactor.netty.http.server.*;
import reactor.netty.http.server.logging.*;
import reactor.netty.tcp.SslProvider;
import reactor.util.annotation.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.constants.NetworkConstants.*;
import static io.art.core.constants.StringConstants.*;
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
    private Predicate<AccessLogArgProvider> accessLogFilter = ignored -> true;
    private AccessLogFactory accessLogFormatFunction;
    private int fragmentationMtu = 0;
    private DataFormat defaultDataFormat = JSON;
    private final HttpServiceExceptionMappingConfigurator exceptionMapping = new HttpServiceExceptionMappingConfigurator();
    private UnaryOperator<HttpRequestDecoderSpec> requestDecoderConfigurator = identity();
    private Http2SslContextSpec defaultSslContext;
    private final Map<String, Consumer<? super SslProvider.SslContextSpec>> sniMapping = map();
    private boolean redirectToHttps = false;
    private final Map<ChannelOption<?>, Object> tcpOptions = map();
    private final HttpServerAuthenticationConfigurator authentication = new HttpServerAuthenticationConfigurator();



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

    public HttpServerModelConfigurator authentication(UnaryOperator<HttpServerAuthenticationConfigurator> configurator) {
        configurator.apply(authentication);
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

    public <O> HttpServerModelConfigurator tcpOption(ChannelOption<O> key, @Nullable O value){
        tcpOptions.put(key, value);
        return this;
    }

    public HttpServerModelConfigurator configureRequestDecoder(UnaryOperator<HttpRequestDecoderSpec> configurator){
        requestDecoderConfigurator = configurator;
        return this;
    }

    @SneakyThrows
    public HttpServerModelConfigurator ssl(File certificate, File key){
        defaultSslContext = Http2SslContextSpec.forServer(certificate, key);
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

    public HttpServerModelConfigurator accessLogFilter(Predicate<AccessLogArgProvider> accessLogFilter){
        this.accessLogFilter = accessLogFilter;
        return this;
    }

    public HttpServerModelConfigurator accessLogFormat(AccessLogFactory formatFunction){
        this.accessLogFormatFunction = formatFunction;
        return this;
    }



    public HttpServerModelConfigurator exception(Class<? extends Throwable> exceptionClass, Function<? extends Throwable, ?> mapper){
        exceptionMapping.on(exceptionClass, mapper);
        return this;
    }

    public HttpServerModelConfigurator exception(Class<? extends Throwable> exceptionClass, HttpResponseStatus httpStatus, Supplier<Object> responseSupplier){
        exceptionMapping.on(exceptionClass, httpStatus, responseSupplier);
        return this;
    }

    public HttpServerModelConfigurator exception(Class<? extends Throwable> exceptionClass, HttpResponseStatus httpStatus){
        return exception(exceptionClass, httpStatus, () -> null);
    }

    public HttpServerModelConfigurator exception(Class<? extends Throwable> exceptionClass, Integer httpStatus, Supplier<Object> responseSupplier){
        return exception(exceptionClass, HttpResponseStatus.valueOf(httpStatus), responseSupplier);
    }

    public HttpServerModelConfigurator exception(Class<? extends Throwable> exceptionClass, Integer httpStatus){
        return exception(exceptionClass, httpStatus, () -> null);
    }



    protected HttpServerModel configure() {
        ImmutableMap.Builder<String, HttpServiceModel> services = immutableMapBuilder();
        routes.forEach((path, modelConfigurator) -> services.put(path, modelConfigurator.configure(path)));
        return HttpServerModel.builder()
                .services(services.build())
                .host(host)
                .port(port)
                .protocol(isNull(defaultSslContext) ? protocol : HttpProtocol.H2)
                .compression(compression)
                .logging(logging)
                .wiretap(wiretap)
                .accessLogging(accessLogging)
                .accessLogFilter(accessLogFilter)
                .accessLogFormatFunction(accessLogFormatFunction)
                .fragmentationMtu(fragmentationMtu)
                .defaultDataFormat(defaultDataFormat)
                .requestDecoderConfigurator(requestDecoderConfigurator)
                .redirectToHttps(redirectToHttps)
                .sslConfigurator(isNull(defaultSslContext) ? null :
                        spec -> spec.sslContext(defaultSslContext)
                                .addSniMappings(sniMapping)
                                .build())
                .exceptionsMapper(exceptionMapping.configure())
                .tcpOptions(tcpOptions)
                .authentication(authentication.getRouter())
                .build();
    }

    private void addRouteIfAbsent(String route, HttpServiceModelConfigurator routeConfigurator){
        if (!existentIDs.contains(routeConfigurator.getId()) && !routes.containsKey(route)){
            existentIDs.add(routeConfigurator.getId());
            routes.put(route, routeConfigurator);
        }
    }

}
